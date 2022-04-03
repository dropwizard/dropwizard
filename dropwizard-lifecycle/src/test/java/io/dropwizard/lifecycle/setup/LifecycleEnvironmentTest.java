package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.mockito.Mockito.mock;

class LifecycleEnvironmentTest {

    private final LifecycleEnvironment environment = new LifecycleEnvironment(new MetricRegistry());

    @Test
    void managesLifeCycleObjects() {
        final LifeCycle lifeCycle = mock(LifeCycle.class);
        environment.manage(lifeCycle);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        assertThat(container.getBeans())
            .contains(lifeCycle);
    }

    @Test
    void managesManagedObjects() {
        final Managed managed = mock(Managed.class);
        environment.manage(managed);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        assertThat(container.getBeans())
            .singleElement()
            .isInstanceOfSatisfying(JettyManaged.class, jettyManaged ->
                assertThat(jettyManaged.getManaged()).isSameAs(managed));
    }

    @Test
    void scheduledExecutorServiceBuildsDaemonThreads() {
        final ScheduledExecutorService executorService = environment.scheduledExecutorService("daemon-%d", true).build();

        assertThat(executorService.submit(() -> Thread.currentThread().isDaemon()))
            .succeedsWithin(1, TimeUnit.SECONDS, as(BOOLEAN))
            .isTrue();
    }

    @Test
    void scheduledExecutorServiceBuildsUserThreadsByDefault() {
        final ScheduledExecutorService executorService = environment.scheduledExecutorService("user-%d").build();

        assertThat(executorService.submit(() -> Thread.currentThread().isDaemon()))
            .succeedsWithin(1, TimeUnit.SECONDS, as(BOOLEAN))
            .isFalse();
    }

    @Test
    void scheduledExecutorServiceThreadFactory() {
        final String expectedName = "DropWizard ThreadFactory Test";
        final String expectedNamePattern = expectedName + "-%d";

        final ThreadFactory tfactory = buildThreadFactory(expectedNamePattern);

        final ScheduledExecutorService executorService = environment.scheduledExecutorService("DropWizard Service", tfactory).build();

        assertThat(executorService.submit(() -> Thread.currentThread().getName()))
            .succeedsWithin(1, TimeUnit.SECONDS, as(STRING))
            .startsWith(expectedName);
    }

    @Test
    void executorServiceThreadFactory() {
        final String expectedName = "DropWizard ThreadFactory Test";
        final String expectedNamePattern = expectedName + "-%d";

        final ThreadFactory tfactory = buildThreadFactory(expectedNamePattern);

        final ExecutorService executorService = environment.executorService("Dropwizard Service", tfactory).build();

        assertThat(executorService.submit(() -> Thread.currentThread().getName()))
            .succeedsWithin(1, TimeUnit.SECONDS, as(STRING))
            .startsWith(expectedName);
    }

    private ThreadFactory buildThreadFactory(String expectedNamePattern) {
        return new ThreadFactory() {
            final AtomicLong counter = new AtomicLong(0L);
            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(false);
                thread.setName(String.format(expectedNamePattern, counter.incrementAndGet()));
                return thread;
            }
        };
    }
}
