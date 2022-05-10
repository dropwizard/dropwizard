package io.dropwizard.cli;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentCommandTest
{
    @SuppressWarnings("NullAway")
    private class TestApplication<T extends Configuration>
        extends Application<T>
    {
        Environment environment;

        @Override
        protected void addDefaultCommands(Bootstrap<T> bootstrap) {
            ServerCommand serverCommand = new ServerCommand<T>(this)
            {
                @Override
                protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
                    super.run(bootstrap, namespace, configuration);
                    TestApplication.this.environment = this.getEnvironment();
                }

                @Override
                public void onError(Cli cli, Namespace namespace, Throwable t) {
                    throw new IllegalStateException("Fatal error trying to start server", t);
                }
            };
            bootstrap.addCommand(serverCommand);
        }

        @Override
        public void run(final T configuration, final Environment environment) throws Exception {
        }

        public Environment getEnvironment() {
            return this.environment;
        }
    }

    @Test
    public void testHealthCheckOnAdminServlet() throws Exception {
        File configFile = File.createTempFile("env-cmd-test", ".yml");
        try {
            Files.write(configFile.toPath(), Arrays.asList(
                "health:",
                "  onAdminServlet: true",
                "server:",
                "  applicationConnectors:",
                "    - type: http",
                "      port: 0",
                "  adminConnectors:",
                "    - type: http",
                "      port: 0"
            ));
            TestApplication testApplication = new TestApplication();
            testApplication.run("server", configFile.getAbsolutePath());
            ServletEnvironment adminEnvironment = testApplication.getEnvironment().admin();
            Set<String> servlets = getServletNames(adminEnvironment);
            assertThat(servlets).contains("health-check-TestApplication-servlet");
            ServletEnvironment servletEnvironment = testApplication.getEnvironment().servlets();
            servlets = getServletNames(servletEnvironment);
            assertThat(servlets).doesNotContain("health-check-TestApplication-servlet");
        }
        finally {
            configFile.delete();
        }
    }

    @Test
    public void testHealthCheckOnApplicationServlet() throws Exception {
        File configFile = File.createTempFile("env-cmd-test", ".yml");
        try {
            Files.write(configFile.toPath(), Arrays.asList(
                "health:",
                "  onAdminServlet: false",
                "server:",
                "  applicationConnectors:",
                "    - type: http",
                "      port: 0",
                "  adminConnectors:",
                "    - type: http",
                "      port: 0"
            ));
            TestApplication testApplication = new TestApplication();
            testApplication.run("server", configFile.getAbsolutePath());
            ServletEnvironment adminEnvironment = testApplication.getEnvironment().admin();
            Set<String> servlets = getServletNames(adminEnvironment);
            assertThat(servlets).doesNotContain("health-check-TestApplication-servlet");
            ServletEnvironment servletEnvironment = testApplication.getEnvironment().servlets();
            servlets = getServletNames(servletEnvironment);
            assertThat(servlets).contains("health-check-TestApplication-servlet");
        }
        finally {
            configFile.delete();
        }
    }

    private Set<String> getServletNames(ServletEnvironment environment) throws Exception {
        Field servletsField = ServletEnvironment.class.getDeclaredField("servlets");
        servletsField.setAccessible(true);
        return (Set<String>) servletsField.get(environment);
    }
}
