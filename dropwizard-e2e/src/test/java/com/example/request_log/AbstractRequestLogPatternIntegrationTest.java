package com.example.request_log;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.List;

@ExtendWith(DropwizardExtensionsSupport.class)
public abstract class AbstractRequestLogPatternIntegrationTest {

    public static class TestApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TestApplication().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(TestResource.class);
            environment.healthChecks().register("dummy", new HealthCheck() {
                @Override
                protected Result check() throws Exception {
                    return Result.healthy();
                }
            });
        }
    }

    @Path("/greet")
    public static class TestResource {
        @GET
        public String get(@QueryParam("name") String name, @Context HttpServletRequest httpRequest) {
            return String.format("Hello, %s!", name);
        }
    }

    @TempDir
    static java.nio.file.Path tempDir;

    protected java.nio.file.Path requestLogFile = tempDir.resolve("request-logs");
    protected Client client;

    DropwizardAppExtension<Configuration> dropwizardAppRule = new DropwizardAppExtension<>(TestApplication.class,
        ResourceHelpers.resourceFilePath("request_log/test-custom-request-log.yml"),
        configOverrides().toArray(new ConfigOverride[0]));

    protected List<ConfigOverride> configOverrides() {
        return Collections.singletonList(ConfigOverride.config("server.requestLog.appenders[0].currentLogFilename", requestLogFile.toString()));
    }

    @BeforeEach
    public void setUp() {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeout(Duration.seconds(2));
        client = new JerseyClientBuilder(dropwizardAppRule.getEnvironment())
            .using(configuration)
            .build("test-request-logs");
    }

    @AfterEach
    public void tearDown() {
        client.close();
    }
}
