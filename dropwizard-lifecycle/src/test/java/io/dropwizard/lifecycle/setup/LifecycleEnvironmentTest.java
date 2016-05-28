package io.dropwizard.lifecycle.setup;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

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

    @Test
    public void scheduledExecutorServiceThreadFactory() throws ExecutionException, InterruptedException {
        final String expectedName = "DropWizard ThreadFactory Test";
        final String expectedNamePattern = expectedName + "-%d";

        final ThreadFactory tfactory = (new ThreadFactoryBuilder())
            .setDaemon(false)
            .setNameFormat(expectedNamePattern)
            .build();

        final ScheduledExecutorService executorService = environment.scheduledExecutorService("DropWizard Service", tfactory).build();
        final Future<Boolean> isFactoryInUse = executorService.submit(() -> Thread.currentThread().getName().startsWith(expectedName));

        assertThat(isFactoryInUse.get()).isTrue();
    }

    @Test
    public void executorServiceThreadFactory() throws ExecutionException, InterruptedException {
        final String expectedName = "DropWizard ThreadFactory Test";
        final String expectedNamePattern = expectedName + "-%d";

        final ThreadFactory tfactory = (new ThreadFactoryBuilder())
            .setDaemon(false)
            .setNameFormat(expectedNamePattern)
            .build();

        final ExecutorService executorService = environment.executorService("Dropwizard Service", tfactory).build();
        final Future<Boolean> isFactoryInUse = executorService.submit(() -> Thread.currentThread().getName().startsWith(expectedName));

        assertThat(isFactoryInUse.get()).isTrue();
    }
}
