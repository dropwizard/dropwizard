package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import com.codahale.dropwizard.servlets.tasks.Task;
import com.codahale.dropwizard.servlets.tasks.TaskServlet;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintWriter;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AdminEnvironmentTest {
    private final MutableServletContextHandler handler = mock(MutableServletContextHandler.class);
    private final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);

    private final AdminEnvironment env = new AdminEnvironment(handler, healthCheckRegistry);

    @Test
    public void addsATaskServlet() throws Exception {
        final Task task = new Task("thing") {
            @Override
            public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            }
        };
        env.addTask(task);

        final ArgumentCaptor<NonblockingServletHolder> captor = ArgumentCaptor.forClass(NonblockingServletHolder.class);

        verify(handler).addServlet(captor.capture(), eq("/tasks/*"));

        final NonblockingServletHolder holder = captor.getValue();
        assertThat(holder.getServlet())
                .isInstanceOf(TaskServlet.class);

        final TaskServlet servlet = (TaskServlet) holder.getServlet();
        assertThat(servlet.getTasks())
                .contains(task);
    }
}
