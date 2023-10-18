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
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
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
        final AtomicBoolean isVirtualThread = new AtomicBoolean(false);

        Environment environment = new Environment("VirtualThreadsTest", Jackson.newMinimalObjectMapper(),
            Validators.newValidatorFactory(), new MetricRegistry(), this.getClass().getClassLoader(),
            new HealthCheckRegistry(), new VirtualThreadsConfiguration());
        DefaultServerFactory defaultServerFactory = new DefaultServerFactory();
        defaultServerFactoryConsumer.accept(defaultServerFactory);
        Server server = defaultServerFactory.build(environment);
        server.start();
        try {
            ThreadPool threadPool = threadPoolSelector.apply(server);
            threadPool.execute(
                () -> isVirtualThread.set(VirtualThreads.isVirtualThread())
            );
        } finally {
            server.stop();
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
