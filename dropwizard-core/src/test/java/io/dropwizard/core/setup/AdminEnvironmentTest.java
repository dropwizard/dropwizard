package io.dropwizard.core.setup;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRegistration;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;

class AdminEnvironmentTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final MutableServletContextHandler handler = new MutableServletContextHandler();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final AdminFactory adminFactory = new AdminFactory();
    private final AdminEnvironment env =
            new AdminEnvironment(handler, healthCheckRegistry, metricRegistry, adminFactory);

    @Test
    void addsATaskServlet() throws Exception {
        final Task task = new Task("thing") {
            @Override
            public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {}
        };
        env.addTask(task);

        handler.setServer(new Server());
        handler.start();

        final ServletRegistration registration =
                handler.getServletHandler().getServletContext().getServletRegistration("tasks");
        assertThat(registration.getMappings()).containsOnly("/tasks/*");
    }
}
