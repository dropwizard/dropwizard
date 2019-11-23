package io.dropwizard.client;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.assertj.core.api.AbstractLongAssert;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardApacheConnectorTest {

    private static final int SLEEP_TIME_IN_MILLIS = 1000;
    private static final int DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 500;
    private static final int ERROR_MARGIN_IN_MILLIS = 300;
    private static final int INCREASE_IN_MILLIS = 100;
    private static final URI NON_ROUTABLE_ADDRESS = URI.create("http://10.255.255.1");

    private static final DropwizardAppExtension<Configuration> APP_RULE = new DropwizardAppExtension<>(
            TestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/dropwizardApacheConnectorTest.yml"));


    private final URI testUri = URI.create("http://localhost:" + APP_RULE.getLocalPort());

    private JerseyClient client;
    private Environment environment;

    @BeforeEach
    void setup() throws Exception {
        JerseyClientConfiguration clientConfiguration = new JerseyClientConfiguration();
        clientConfiguration.setConnectionTimeout(Duration.milliseconds(SLEEP_TIME_IN_MILLIS / 2));
        clientConfiguration.setTimeout(Duration.milliseconds(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS));

        environment = new Environment("test-dropwizard-apache-connector");
        client = (JerseyClient) new JerseyClientBuilder(environment)
                .using(clientConfiguration)
                .build("test");
        for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
            lifeCycle.start();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
            lifeCycle.stop();
        }
        assertThat(client.isClosed()).isTrue();
    }

    @Test
    void when_no_read_timeout_override_then_client_request_times_out() {
        assertThatThrownBy(() ->client.target(testUri + "/long_running").request().get())
                .isInstanceOf(ProcessingException.class)
                .hasCauseInstanceOf(SocketTimeoutException.class);
    }

    @Test
    void when_read_timeout_override_created_then_client_requests_completes_successfully() {
        client.target(testUri + "/long_running")
                .property(ClientProperties.READ_TIMEOUT, SLEEP_TIME_IN_MILLIS * 2)
                .request()
                .get();
    }

    /**
     * <p>In first assertion we prove, that a request takes no longer than:
     * <em>request_time < connect_timeout + error_margin</em> (1)</p>
     * <p/>
     * </p>In the second we show that if we set <b>connect_timeout</b> to
     * <b>set_connect_timeout + increase + error_margin</b> then
     * <em>request_time > connect_timeout + increase + error_margin</em> (2)</p>
     * <p/>
     * <p>Now, (1) and (2) can hold at the same time if then connect_timeout update was successful.</p>
     */
    @Test
    @Disabled("Flaky, timeout jumps over the threshold")
    void connect_timeout_override_changes_how_long_it_takes_for_a_connection_to_timeout() {
        // setUp override
        WebTarget target = client.target(NON_ROUTABLE_ADDRESS);

        //This can't be tested without a real connection
        try {
            target.request().get(Response.class);
        } catch (ProcessingException e) {
            if (e.getCause() instanceof HttpHostConnectException) {
                return;
            }
        }

        assertThatConnectionTimeoutFor(target).isLessThan(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + ERROR_MARGIN_IN_MILLIS);

        // tearDown override
        final int newTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + INCREASE_IN_MILLIS + ERROR_MARGIN_IN_MILLIS;
        final WebTarget newTarget = target.property(ClientProperties.CONNECT_TIMEOUT, newTimeout);
        assertThatConnectionTimeoutFor(newTarget).isGreaterThan(newTimeout);
    }

    @Test
    void when_no_override_then_redirected_request_successfully_redirected() {
        assertThat(client.target(testUri + "/redirect")
                        .request()
                        .get(String.class)
        ).isEqualTo("redirected");
    }

    @Test
    void when_configuration_overridden_to_disallow_redirects_temporary_redirect_status_returned() {
        assertThat(client.target(testUri + "/redirect")
                        .property(ClientProperties.FOLLOW_REDIRECTS, false)
                        .request()
                        .get(Response.class)
                        .getStatus()
        ).isEqualTo(HttpStatus.SC_TEMPORARY_REDIRECT);
    }

    @Test
    void when_jersey_client_runtime_is_garbage_collected_apache_client_is_not_closed() {
        for (int j = 0; j < 5; j++) {
            System.gc(); // We actually want GC here
            final String response = client.target(testUri + "/long_running")
                    .property(ClientProperties.READ_TIMEOUT, SLEEP_TIME_IN_MILLIS * 2)
                    .request()
                    .get(String.class);
            assertThat(response).isEqualTo("success");
        }
    }

    @Test
    void multiple_headers_with_the_same_name_are_processed_successfully() throws Exception {

        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        final DropwizardApacheConnector dropwizardApacheConnector = new DropwizardApacheConnector(client, null, false);
        final Header[] apacheHeaders = {
            new BasicHeader("Set-Cookie", "test1"),
            new BasicHeader("Set-Cookie", "test2")
        };

        final CloseableHttpResponse apacheResponse = mock(CloseableHttpResponse.class);
        when(apacheResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        when(apacheResponse.getAllHeaders()).thenReturn(apacheHeaders);
        when(client.execute(Mockito.any())).thenReturn(apacheResponse);

        final ClientRequest jerseyRequest = mock(ClientRequest.class);
        when(jerseyRequest.getUri()).thenReturn(URI.create("http://localhost"));
        when(jerseyRequest.getMethod()).thenReturn("GET");
        when(jerseyRequest.getHeaders()).thenReturn(new MultivaluedHashMap<>());

        final ClientResponse jerseyResponse = dropwizardApacheConnector.apply(jerseyRequest);

        assertThat(jerseyResponse.getStatus()).isEqualTo(apacheResponse.getStatusLine().getStatusCode());

    }

    @Path("/")
    public static class TestResource {

        @GET
        @Path("/long_running")
        public String getWithSleep() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(SLEEP_TIME_IN_MILLIS);
            return "success";
        }

        @GET
        @Path("redirect")
        public Response getWithRedirect() {
            return Response.temporaryRedirect(URI.create("/redirected")).build();
        }

        @GET
        @Path("redirected")
        public String redirectedGet() {
            return "redirected";
        }

    }

    public static class TestApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TestApplication().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(TestResource.class);
            environment.healthChecks().register("dummy", new HealthCheck() {
                @Override
                protected Result check() {
                    return Result.healthy();
                }
            });
        }
    }

    private static AbstractLongAssert<?> assertThatConnectionTimeoutFor(WebTarget webTarget) {
        final long startTime = System.nanoTime();
        try {
            webTarget.request().get(Response.class);
        } catch (ProcessingException e) {
            final long endTime = System.nanoTime();
            assertThat(e).isNotNull();
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).isInstanceOfAny(ConnectTimeoutException.class, NoRouteToHostException.class);
            return assertThat(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
        }
        throw new AssertionError("ProcessingException expected but not thrown");
    }

}
