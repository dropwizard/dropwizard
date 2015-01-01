package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.servlets.tasks.Task;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import javax.servlet.ServletRegistration;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AdminEnvironmentTest {
    private final MutableServletContextHandler handler = new MutableServletContextHandler();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final AdminEnvironment env = new AdminEnvironment(handler, healthCheckRegistry, metricRegistry);
    private final Task task = new Task("thing") {
        @Override
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        }
    };

    @Test
    public void addsATaskServlet() throws Exception {
        env.addTask(task);

        handler.setServer(new Server());
        handler.start();

        final ServletRegistration registration = handler.getServletHandler()
                .getServletContext()
                .getServletRegistration("tasks");
        assertThat(registration.getMappings())
                .containsOnly("/tasks/*");
    }

    @Test
    public void getsTaskClassCanonicalNameForInnerClass() {
        assertThat(env.getTaskClassName.apply(new A())).isEqualTo(getClass().getCanonicalName() + ".A");
    }

    @Test
    public void getsTaskClassNameForAnonymousClass() {
        assertThat(env.getTaskClassName.apply(task)).isEqualTo(getClass().getCanonicalName() + "$1");
    }

    static class A extends Task {

        protected A() {
            super("a");
        }

        @Override
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            // nothing
        }
    }
}
