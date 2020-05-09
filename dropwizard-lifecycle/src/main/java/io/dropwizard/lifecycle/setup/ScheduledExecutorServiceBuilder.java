package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ScheduledExecutorServiceBuilder {

    private final LifecycleEnvironment environment;
    private final String namePrefix;
    private int poolSize;
    private ThreadFactory threadFactory;
    private Duration shutdownTime;
    private RejectedExecutionHandler handler;
    private boolean removeOnCancel;

    public ScheduledExecutorServiceBuilder(LifecycleEnvironment environment, String namePrefix, ThreadFactory factory) {
        this.environment = environment;
        this.namePrefix = namePrefix;
        this.poolSize = 1;
        this.threadFactory = factory;
        this.shutdownTime = Duration.seconds(5);
        this.handler = new ThreadPoolExecutor.AbortPolicy();
        this.removeOnCancel = false;
    }

    public ScheduledExecutorServiceBuilder(LifecycleEnvironment environment, String namePrefix, boolean useDaemonThreads) {
        this(environment, namePrefix, new NamedThreadFactory(namePrefix, useDaemonThreads));
    }

    public ScheduledExecutorServiceBuilder threads(int threads) {
        this.poolSize = threads;
        return this;
    }

    public ScheduledExecutorServiceBuilder shutdownTime(Duration time) {
        this.shutdownTime = time;
        return this;
    }

    public ScheduledExecutorServiceBuilder rejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    public ScheduledExecutorServiceBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ScheduledExecutorServiceBuilder removeOnCancelPolicy(boolean removeOnCancel) {
        this.removeOnCancel = removeOnCancel;
        return this;
    }

    public ScheduledExecutorService build() {
        final InstrumentedThreadFactory instrumentedThreadFactory = new InstrumentedThreadFactory(threadFactory,
            environment.getMetricRegistry(), namePrefix);

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize, instrumentedThreadFactory, handler);
        executor.setRemoveOnCancelPolicy(removeOnCancel);

        environment.manage(new ExecutorServiceManager(executor, shutdownTime, namePrefix));
        return executor;
    }
}
