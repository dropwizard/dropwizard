package io.dropwizard.lifecycle.setup;

import com.google.common.collect.ImmutableList;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LifecycleEnvironmentTest {
    private final LifecycleEnvironment environment = new LifecycleEnvironment();

    @Test
    public void managesLifeCycleObjects() throws Exception {
        final LifeCycle lifeCycle = mock(LifeCycle.class);
        environment.manage(lifeCycle);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        assertThat(container.getBeans())
                .contains(lifeCycle);
    }

    @Test
    public void managesManagedObjects() throws Exception {
        final Managed managed = mock(Managed.class);
        environment.manage(managed);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        final Object bean = ImmutableList.copyOf(container.getBeans()).get(0);
        assertThat(bean)
                .isInstanceOf(JettyManaged.class);

        final JettyManaged jettyManaged = (JettyManaged) bean;

        assertThat(jettyManaged.getManaged())
                .isEqualTo(managed);
    }

    @Test
    public void scheduledExecutorServiceBuildsDaemonThreads() throws ExecutionException, InterruptedException {
        final ScheduledExecutorService executorService = environment.scheduledExecutorService("daemon-%d", true).build();
        final Future<Boolean> isDaemon = executorService.submit(() -> Thread.currentThread().isDaemon());

        assertThat(isDaemon.get()).isTrue();
    }

    @Test
    public void scheduledExecutorServiceBuildsUserThreadsByDefault() throws ExecutionException, InterruptedException {
        final ScheduledExecutorService executorService = environment.scheduledExecutorService("user-%d").build();
        final Future<Boolean> isDaemon = executorService.submit(() -> Thread.currentThread().isDaemon());

        assertThat(isDaemon.get()).isFalse();
    }
}
