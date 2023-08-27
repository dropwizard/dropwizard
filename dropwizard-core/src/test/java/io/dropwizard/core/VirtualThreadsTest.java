package io.dropwizard.core;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.VirtualThreads;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledForJreRange(min = JRE.JAVA_21)
class VirtualThreadsTest {
    private static class VirtualThreadsConfiguration extends Configuration {
    }

    private final AtomicBoolean isVirtualThread = new AtomicBoolean(false);
    private final Runnable virtualThreadRunnable = () -> isVirtualThread.set(VirtualThreads.isVirtualThread());

    @Test
    void virtualThreadsSupported() throws Exception {
        Environment environment = new Environment("VirtualThreadsTest", Jackson.newMinimalObjectMapper(),
            Validators.newValidatorFactory(), new MetricRegistry(), this.getClass().getClassLoader(),
            new HealthCheckRegistry(), new VirtualThreadsConfiguration());
        DefaultServerFactory defaultServerFactory = new DefaultServerFactory();
        defaultServerFactory.setEnableVirtualThreads(true);
        Server server = defaultServerFactory.build(environment);
        server.start();
        try {
            server.getThreadPool().execute(virtualThreadRunnable);
        } finally {
            server.stop();
        }

        assertThat(isVirtualThread).isTrue();
    }
}
