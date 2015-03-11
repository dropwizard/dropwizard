package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;
import org.assertj.core.api.AbstractLongAssert;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.any;

public class DropwizardApacheConnectorTest {
    private static final int SLEEP_TIME_IN_MILLIS = 500;
    private static final int DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 200;
    private static final int ERROR_MARGIN_IN_MILLIS = 300;
    private static final int INCREASE_IN_MILLIS = 100;
    private static final String NON_ROUTABLE_IP_ADDRESS = "10.255.255.1"; 
    public static final URI URI_THAT_TIMESOUT_ON_CONNECTION_ATTEMPT = URI.create("http://" + NON_ROUTABLE_IP_ADDRESS);
    @ClassRule
    public static DropwizardAppRule<Configuration> APP_RULE =
            new DropwizardAppRule<>(TestApplication.class, Resources.getResource("yaml/dropwizardApacheConnectorTest.yml").getPath());
    public final URI testUri = URI.create("http://localhost:" + APP_RULE.getLocalPort());
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Client client;

    @Before
    public void setup() {
        JerseyClientConfiguration clientConfiguration = new JerseyClientConfiguration();
        clientConfiguration.setConnectionTimeout(Duration.milliseconds(SLEEP_TIME_IN_MILLIS / 2));
        clientConfiguration.setTimeout(Duration.milliseconds(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS));
        client = new JerseyClientBuilder(new MetricRegistry())
                .using(Executors.newSingleThreadExecutor(), Jackson.newObjectMapper())
                .using(clientConfiguration)
                .build("test");
    }

    @Test
    public void when_no_read_timeout_override_then_client_request_times_out() {
        thrown.expect(ProcessingException.class);
        thrown.expectCause(any(SocketTimeoutException.class));

        client.target(testUri + "/long_running")
                .request()
                .get();
    }

    @Test
    public void when_read_timeout_override_created_then_client_requests_completes_successfully() {
        client.target(testUri + "/long_running")
                .property(ClientProperties.READ_TIMEOUT, SLEEP_TIME_IN_MILLIS * 2)
                .request()
                .get();
    }

    /**
     *
     * In first assertion we prove, that a request takes no longer than:
     * request_time < set_connect_timeout + error_margin (1)
     *
     * in the second we show that if we set connect_timeout to set_connect_timeout + error margin + increase
     * then
     *
     * request_time' > set_connect_timeout + error margin + increase (2)
     *
     * Now, (1) and (2) can hold at the same time iff then connect_timeout update was successful.
     */
    @Test
    public void connect_timeout_override_changes_how_long_it_takes_for_a_connection_to_timeout() {
        // before override
        assertThatGetConnectonTimeoutForTarget(
                client.target(URI_THAT_TIMESOUT_ON_CONNECTION_ATTEMPT)
        ).isLessThan(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + ERROR_MARGIN_IN_MILLIS);

        // after override
        assertThatGetConnectonTimeoutForTarget(
                client.target(URI_THAT_TIMESOUT_ON_CONNECTION_ATTEMPT)
                        .property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + INCREASE_IN_MILLIS + ERROR_MARGIN_IN_MILLIS)
        ).isGreaterThan(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + INCREASE_IN_MILLIS + ERROR_MARGIN_IN_MILLIS);
    }

    @Test
    public void when_no_override_then_redirected_request_successfully_redirected() {
        assertThat(
                client.target(testUri + "/redirect")
                        .request()
                        .get(String.class)
        ).isEqualTo("redirected");
    }

    @Test
    public void when_configuration_overridden_to_disallow_redirects_temporary_redirect_status_returned() {
        assertThat(
                client.target(testUri + "/redirect")
                        .property(ClientProperties.FOLLOW_REDIRECTS, false)
                        .request()
                        .get(Response.class)
                        .getStatus()
        ).isEqualTo(HttpStatus.SC_TEMPORARY_REDIRECT);
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
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(TestResource.class);
        }
    }

    private static AbstractLongAssert<?> assertThatGetConnectonTimeoutForTarget(WebTarget webTarget) {
        final long startTime = System.nanoTime();
        try {
            webTarget
                    .request()
                    .get(Response.class);
        } catch (ProcessingException e) {
            final long endTime = System.nanoTime();
            assertThat(e).isNotNull();
            //noinspection ConstantConditions
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).isInstanceOf(ConnectTimeoutException.class);
            return assertThat((endTime - startTime)/1000000);
        }
        throw new AssertionError("ProcessingException expected but not thrown");
    }

}