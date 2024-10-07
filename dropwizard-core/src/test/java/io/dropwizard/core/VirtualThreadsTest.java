package io.dropwizard.core;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.VirtualThreads;
import org.eclipse.jetty.util.thread.ExecutionStrategy;
import org.eclipse.jetty.util.thread.Invocable;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.util.thread.strategy.AdaptiveExecutionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledForJreRange(min = JRE.JAVA_21)
class VirtualThreadsTest {
    private static class VirtualThreadsConfiguration extends Configuration {
    }

    @Test
    void virtualThreadsEnabledWhenRequested() throws Exception {
        boolean isVirtualThread = probeVirtualThread(
            defaultServerFactory -> defaultServerFactory.setEnableVirtualThreads(true),
            this::selectServerThreadPool
        );

        assertThat(isVirtualThread).isTrue();
    }

    @Test
    void virtualThreadsDisabledWhenNotRequested() throws Exception {
        boolean isVirtualThread = probeVirtualThread(
            defaultServerFactory -> defaultServerFactory.setEnableVirtualThreads(false),
            this::selectServerThreadPool
        );

        assertThat(isVirtualThread).isFalse();
    }

    @Test
    void virtualAdminThreadsEnabledWhenRequested() throws Exception {
        boolean isVirtualThread = probeVirtualThread(
            defaultServerFactory -> defaultServerFactory.setEnableAdminVirtualThreads(true),
            this::selectAdminThreadPool
        );

        assertThat(isVirtualThread).isTrue();
    }

    @Test
    void virtualAdminThreadsDisabledWhenNotRequested() throws Exception {
        boolean isVirtualThread = probeVirtualThread(
            defaultServerFactory -> defaultServerFactory.setEnableAdminVirtualThreads(false),
            this::selectAdminThreadPool
        );

        assertThat(isVirtualThread).isFalse();
    }

    private boolean probeVirtualThread(Consumer<DefaultServerFactory> defaultServerFactoryConsumer,
                                       Function<Server, ThreadPool> threadPoolSelector) throws Exception {
        final AtomicReference<Boolean> isVirtualThread = new AtomicReference<>(null);

        Environment environment = new Environment("VirtualThreadsTest", Jackson.newMinimalObjectMapper(),
            Validators.newValidatorFactory(), new MetricRegistry(), this.getClass().getClassLoader(),
            new HealthCheckRegistry(), new VirtualThreadsConfiguration());
        DefaultServerFactory defaultServerFactory = new DefaultServerFactory();
        defaultServerFactoryConsumer.accept(defaultServerFactory);
        Server server = defaultServerFactory.build(environment);
        server.start();
        ExecutionStrategy.Producer producer = () -> {
            if (isVirtualThread.get() != null) {
                return null;
            }
            return Invocable.from(Invocable.InvocationType.BLOCKING, () -> isVirtualThread.set(VirtualThreads.isVirtualThread()));
        };
        AdaptiveExecutionStrategy adaptiveExecutionStrategy = new AdaptiveExecutionStrategy(producer, threadPoolSelector.apply(server));
        adaptiveExecutionStrategy.start();
        try {
            adaptiveExecutionStrategy.dispatch();
            while (isVirtualThread.get() == null) {
                Thread.yield();
            }
        } finally {
            adaptiveExecutionStrategy.stop();
            server.stop();
        }

        if (isVirtualThread.get() == null) {
            throw new IllegalStateException("Didn't execute virtual thread probe");
        }
        return isVirtualThread.get();
    }

    private ThreadPool selectServerThreadPool(Server server) {
        return server.getThreadPool();
    }

    private ThreadPool selectAdminThreadPool(Server server) {
        final int adminPort = 8081;
        return Arrays.stream(server.getConnectors())
            .filter(ServerConnector.class::isInstance)
            .map(ServerConnector.class::cast)
            .filter(serverConnector -> serverConnector.getLocalPort() == adminPort)
            .map(AbstractConnector::getExecutor)
            .filter(ThreadPool.class::isInstance)
            .map(ThreadPool.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Couldn't find thread pool of admin connector"));
    }
}
