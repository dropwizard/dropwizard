package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.JettyManaged;
import com.codahale.dropwizard.lifecycle.ExecutorServiceManager;
import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.dropwizard.lifecycle.ServerLifecycleListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class LifecycleEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEnvironment.class);

    private final List<Object> managedObjects;
    private final List<LifeCycle.Listener> lifecycleListeners;

    public LifecycleEnvironment(List<Object> managedObjects,
                                List<LifeCycle.Listener> lifecycleListeners) {
        this.managedObjects = managedObjects;
        this.lifecycleListeners = lifecycleListeners;
    }

    /**
     * Adds the given {@link com.codahale.dropwizard.lifecycle.Managed} instance to the set of objects
     * managed by the server's lifecycle. When the server starts, {@code managed} will be started.
     * When the server stops, {@code managed} will be stopped.
     *
     * @param managed a managed object
     */
    public void manage(Managed managed) {
        managedObjects.add(new JettyManaged(checkNotNull(managed)));
    }

    /**
     * Adds the given Jetty {@link org.eclipse.jetty.util.component.LifeCycle} instances to the
     * server's lifecycle.
     *
     * @param managed a Jetty-managed object
     */
    public void manage(LifeCycle managed) {
        managedObjects.add(checkNotNull(managed));
    }

    public ExecutorServiceBuilder executorService(String nameFormat) {
        return new ExecutorServiceBuilder(managedObjects, nameFormat);
    }

    public ScheduledExecutorServiceBuilder scheduledExecutorService(String nameFormat) {
        return new ScheduledExecutorServiceBuilder(managedObjects, nameFormat);
    }

    /**
     * Creates a new {@link java.util.concurrent.ExecutorService} instance with the given parameters
     * whose lifecycle is managed by the service.
     *
     * @param nameFormat      a {@link String#format(String, Object...)}-compatible format String,
     *                        to which a unique integer (0, 1, etc.) will be supplied as the single
     *                        parameter.
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime   when the number of threads is greater than the core, this is the
     *                        maximum time that excess idle threads will wait for new tasks before
     *                        terminating.
     * @param unit            the time unit for the keepAliveTime argument.
     * @return a new {@link java.util.concurrent.ExecutorService} instance
     */
    public ExecutorService managedExecutorService(String nameFormat,
                                                  int corePoolSize,
                                                  int maximumPoolSize,
                                                  long keepAliveTime,
                                                  TimeUnit unit) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat)
                                                                      .build();
        final ExecutorService executor = new ThreadPoolExecutor(corePoolSize,
                                                                maximumPoolSize,
                                                                keepAliveTime,
                                                                unit,
                                                                new LinkedBlockingQueue<Runnable>(),
                                                                threadFactory);
        managedObjects.add(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS, nameFormat));
        return executor;
    }

    /**
     * Creates a new {@link ScheduledExecutorService} instance with the given parameters whose
     * lifecycle is managed by the service.
     *
     * @param nameFormat   a {@link String#format(String, Object...)}-compatible format String, to
     *                     which a unique integer (0, 1, etc.) will be supplied as the single
     *                     parameter.
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @return a new {@link ScheduledExecutorService} instance
     */
    public ScheduledExecutorService managedScheduledExecutorService(String nameFormat,
                                                                    int corePoolSize) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat)
                                                                      .build();
        final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(corePoolSize,
                                                                                  threadFactory);
        managedObjects.add(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS, nameFormat));
        return executor;
    }

    public void addServerLifecycleListener(ServerLifecycleListener listener) {
        lifecycleListeners.add(new ServerListener(listener));
    }

    public void addLifeCycleListener(LifeCycle.Listener listener) {
        lifecycleListeners.add(listener);
    }

    private static class ServerListener extends AbstractLifeCycle.AbstractLifeCycleListener {
        private final ServerLifecycleListener listener;

        private ServerListener(ServerLifecycleListener listener) {
            this.listener = listener;
        }

        @Override
        public void lifeCycleStarted(LifeCycle event) {
            if (event instanceof Server) {
                listener.serverStarted((Server) event);
            }
        }
    }
}
