package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class JerseyIgnoreRequestUserAgentHeaderFilterTest {
    public static final DropwizardAppExtension<Configuration> APP_RULE =
            new DropwizardAppExtension<>(TestApplication.class, JerseyIgnoreRequestUserAgentHeaderFilterTest.class.getResource("/yaml/jerseyIgnoreRequestUserAgentHeaderFilterTest.yml").getPath());

    private final URI testUri = URI.create("http://localhost:" + APP_RULE.getLocalPort());
    private JerseyClientBuilder clientBuilder;
    private JerseyClientConfiguration clientConfiguration;

    @BeforeEach
    void setup() {
        clientConfiguration = new JerseyClientConfiguration();
        clientConfiguration.setConnectionTimeout(Duration.milliseconds(1000L));
        clientConfiguration.setTimeout(Duration.milliseconds(2500L));
        clientBuilder = new JerseyClientBuilder(new MetricRegistry())
            .using(clientConfiguration)
            .using(Executors.newSingleThreadExecutor(), Jackson.newObjectMapper());
    }

    @Test
    void clientIsSetRequestIsNotSet() {
        clientConfiguration.setUserAgent(Optional.of("ClientUserAgentHeaderValue"));
        assertThat(
                clientBuilder.using(clientConfiguration).
                build("ClientName").target(testUri + "/user_agent")
                        .request()
                        .get(String.class)
        ).isEqualTo("ClientUserAgentHeaderValue");
    }

    @Test
    void clientIsNotSetRequestIsSet() {
        assertThat(
                clientBuilder.build("ClientName").target(testUri + "/user_agent")
                        .request().header("User-Agent", "RequestUserAgentHeaderValue")
                        .get(String.class)
        ).isEqualTo("RequestUserAgentHeaderValue");
    }

    @Test
    void clientIsNotSetRequestIsNotSet() {
        assertThat(
                clientBuilder.build("ClientName").target(testUri + "/user_agent")
                        .request()
                        .get(String.class)
        ).isEqualTo("ClientName");
    }

    @Test
    void clientIsSetRequestIsSet() {
        clientConfiguration.setUserAgent(Optional.of("ClientUserAgentHeaderValue"));
        assertThat(
                clientBuilder.build("ClientName").target(testUri + "/user_agent")
                        .request().header("User-Agent", "RequestUserAgentHeaderValue")
                        .get(String.class)
        ).isEqualTo("RequestUserAgentHeaderValue");
    }

    @Path("/")
    public static class TestResource {
        @GET
        @Path("user_agent")
        public String getReturnUserAgentHeader(@HeaderParam("User-Agent") String userAgentHeader) {
            return userAgentHeader;
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
}
