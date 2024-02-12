package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.util.Duration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.util.Objects.requireNonNull;

public class LifecycleEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEnvironment.class);

    private final List<LifeCycle> managedObjects;
    private final List<LifeCycle.Listener> lifecycleListeners;
    private final MetricRegistry metricRegistry;

    public LifecycleEnvironment(MetricRegistry metricRegistry) {
        this.managedObjects = new ArrayList<>();
        this.lifecycleListeners = new ArrayList<>();
        this.metricRegistry = metricRegistry;
    }

    public List<LifeCycle> getManagedObjects() {
        return managedObjects;
    }

    /**
     * Adds the given {@link Managed} instance to the set of objects managed by the server's
     * lifecycle. When the server starts, {@code managed} will be started. When the server stops,
     * {@code managed} will be stopped.
     *
     * @param managed a managed object
     */
    public void manage(Managed managed) {
        managedObjects.add(new JettyManaged(requireNonNull(managed)));
    }

    /**
     * Adds the given Jetty {@link LifeCycle} instances to the server's lifecycle.
     *
     * @param managed a Jetty-managed object
     */
    public void manage(LifeCycle managed) {
        managedObjects.add(requireNonNull(managed));
    }

    public ExecutorServiceBuilder executorService(String nameFormat) {
        return new ExecutorServiceBuilder(this, nameFormat);
    }

    public ExecutorServiceBuilder executorService(String nameFormat, ThreadFactory factory) {
        return new ExecutorServiceBuilder(this, nameFormat, factory);
    }

    public ExecutorService virtualExecutorService(String nameFormat) {
        return virtualExecutorService(nameFormat, Duration.seconds(5));
    }

    public ExecutorService virtualExecutorService(String nameFormat, Duration shutdownTime) {
        try {
            Object virtualThreadBuilder = Thread.class.getDeclaredMethod("ofVirtual").invoke(null);
            Object virtualThreadFactory = Class.forName("java.lang.Thread$Builder")
                .getDeclaredMethod("factory")
                .invoke(virtualThreadBuilder);
            InstrumentedThreadFactory factory = new InstrumentedThreadFactory((ThreadFactory) virtualThreadFactory, getMetricRegistry(), nameFormat);
            ExecutorService virtualThreadExecutor = (ExecutorService) Executors.class
                .getDeclaredMethod("newThreadPerTaskExecutor", ThreadFactory.class)
                .invoke(null, factory);
            manage(new ExecutorServiceManager(virtualThreadExecutor, shutdownTime, nameFormat));
            return virtualThreadExecutor;
        } catch (InvocationTargetException invocationTargetException) {
            throw new IllegalStateException("Error while creating virtual executor service", invocationTargetException.getCause());
        } catch (Exception exception) {
            throw new IllegalStateException("Error while creating virtual executor service", exception);
        }
    }

    public ScheduledExecutorServiceBuilder scheduledExecutorService(String nameFormat) {
        return scheduledExecutorService(nameFormat, false);
    }

    public ScheduledExecutorServiceBuilder scheduledExecutorService(String nameFormat, ThreadFactory factory) {
        return new ScheduledExecutorServiceBuilder(this, nameFormat, factory);
    }

    public ScheduledExecutorServiceBuilder scheduledExecutorService(String nameFormat, boolean useDaemonThreads) {
        return new ScheduledExecutorServiceBuilder(this, nameFormat, useDaemonThreads);
    }

    public void addServerLifecycleListener(ServerLifecycleListener listener) {
        lifecycleListeners.add(new ServerListener(listener));
    }

    public void addEventListener(LifeCycle.Listener listener) {
        lifecycleListeners.add(listener);
    }

    public void attach(ContainerLifeCycle container) {
        for (LifeCycle object : managedObjects) {
            container.addBean(object);
        }
        container.addEventListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                LOGGER.debug("managed objects = {}", managedObjects);
            }
        });
        for (LifeCycle.Listener listener : lifecycleListeners) {
            container.addEventListener(listener);
        }
    }

    /**
     * @since 2.0
     */
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    private static class ServerListener implements LifeCycle.Listener {
        private final ServerLifecycleListener listener;

        private ServerListener(ServerLifecycleListener listener) {
            this.listener = listener;
        }

        @Override
        public void lifeCycleStarted(LifeCycle event) {
            if (event instanceof Server server) {
                listener.serverStarted(server);
            }
        }
    }
}
