package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.HttpApplication;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.HttpEnvironment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.assertj.core.api.AbstractLongAssert;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Before;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.Validation;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.any;

public class DropwizardApacheConnectorTest {

    private static final int SLEEP_TIME_IN_MILLIS = 500;
    private static final int DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 200;
    private static final int ERROR_MARGIN_IN_MILLIS = 300;
    private static final int INCREASE_IN_MILLIS = 100;
    private static final URI NON_ROUTABLE_ADDRESS = URI.create("http://10.255.255.1");

    @ClassRule
    public static final DropwizardAppRule<HttpConfiguration> APP_RULE = new DropwizardAppRule<>(
            TestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/dropwizardApacheConnectorTest.yml"));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final URI testUri = URI.create("http://localhost:" + APP_RULE.getLocalPort());

    private JerseyClient client;
    private HttpEnvironment environment;

    @Before
    public void setup() throws Exception {
        JerseyClientConfiguration clientConfiguration = new JerseyClientConfiguration();
        clientConfiguration.setConnectionTimeout(Duration.milliseconds(SLEEP_TIME_IN_MILLIS / 2));
        clientConfiguration.setTimeout(Duration.milliseconds(DEFAULT_CONNECT_TIMEOUT_IN_MILLIS));

        environment = new HttpEnvironment("test-dropwizard-apache-connector", Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(), new MetricRegistry(),
                getClass().getClassLoader());
        client = (JerseyClient) new JerseyClientBuilder(environment)
                .using(clientConfiguration)
                .build("test");
        for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
            lifeCycle.start();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
            lifeCycle.stop();
        }
        assertThat(client.isClosed()).isTrue();
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
    public void connect_timeout_override_changes_how_long_it_takes_for_a_connection_to_timeout() {
        // before override
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

        // after override
        final int newTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MILLIS + INCREASE_IN_MILLIS + ERROR_MARGIN_IN_MILLIS;
        final WebTarget newTarget = target.property(ClientProperties.CONNECT_TIMEOUT, newTimeout);
        assertThatConnectionTimeoutFor(newTarget).isGreaterThan(newTimeout);
    }

    @Test
    public void when_no_override_then_redirected_request_successfully_redirected() {
        assertThat(client.target(testUri + "/redirect")
                        .request()
                        .get(String.class)
        ).isEqualTo("redirected");
    }

    @Test
    public void when_configuration_overridden_to_disallow_redirects_temporary_redirect_status_returned() {
        assertThat(client.target(testUri + "/redirect")
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

    public static class TestApplication extends HttpApplication<HttpConfiguration> {
        public static void main(String[] args) throws Exception {
            new TestApplication().run(args);
        }

        @Override
        public void run(HttpConfiguration configuration, HttpEnvironment environment) throws Exception {
            environment.jersey().register(TestResource.class);
            environment.healthChecks().register("dummy", new HealthCheck() {
                @Override
                protected Result check() throws Exception {
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
            //noinspection ConstantConditions
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).isInstanceOfAny(ConnectTimeoutException.class, NoRouteToHostException.class);
            return assertThat(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));
        }
        throw new AssertionError("ProcessingException expected but not thrown");
    }

}
