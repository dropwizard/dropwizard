package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.servlets.tasks.Task;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import javax.servlet.ServletRegistration;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminEnvironmentTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final MutableServletContextHandler handler = new MutableServletContextHandler();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final AdminEnvironment env = new AdminEnvironment(handler, healthCheckRegistry, metricRegistry);

    @Test
    public void addsATaskServlet() throws Exception {
        final Task task = new Task("thing") {
            @Override
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            }
        };
        env.addTask(task);

        handler.setServer(new Server());
        handler.start();

        final ServletRegistration registration = handler.getServletHandler()
                                                        .getServletContext()
                                                        .getServletRegistration("tasks");
        assertThat(registration.getMappings())
                .containsOnly("/tasks/*");
    }
}
