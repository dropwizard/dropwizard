package com.example.request_log;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

    protected final String tempFile = createTempFile();
    protected Client client;

    @Rule
    public DropwizardAppRule<Configuration> dropwizardAppRule = new DropwizardAppRule<>(TestApplication.class,
        ResourceHelpers.resourceFilePath("request_log/test-custom-request-log.yml"),
        configOverrides().toArray(new ConfigOverride[0]));

    private static String createTempFile() {
        try {
            return File.createTempFile("request-logs", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected List<ConfigOverride> configOverrides() {
        return Collections.singletonList(ConfigOverride.config("server.requestLog.appenders[0].currentLogFilename", tempFile));
    }

    @Before
    public void setUp() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeout(Duration.seconds(2));
        client = new JerseyClientBuilder(dropwizardAppRule.getEnvironment())
            .using(configuration)
            .build("test-request-logs");
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }
}
