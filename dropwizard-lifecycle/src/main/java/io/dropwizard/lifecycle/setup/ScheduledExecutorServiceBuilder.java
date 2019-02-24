package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduledExecutorServiceBuilder {

    private static final AtomicLong COUNT = new AtomicLong(0);
    private final LifecycleEnvironment environment;
    private final String nameFormat;
    private int poolSize;
    private ThreadFactory threadFactory;
    private Duration shutdownTime;
    private RejectedExecutionHandler handler;
    private boolean removeOnCancel;

    public ScheduledExecutorServiceBuilder(LifecycleEnvironment environment, String nameFormat, ThreadFactory factory) {
        this.environment = environment;
        this.nameFormat = nameFormat;
        this.poolSize = 1;
        this.threadFactory = factory;
        this.shutdownTime = Duration.seconds(5);
        this.handler = new ThreadPoolExecutor.AbortPolicy();
        this.removeOnCancel = false;
    }

    public ScheduledExecutorServiceBuilder(LifecycleEnvironment environment, String nameFormat, boolean useDaemonThreads) {
        this(environment, nameFormat, buildThreadFactory(nameFormat, useDaemonThreads));
    }

    private static ThreadFactory buildThreadFactory(String nameFormat, boolean daemon) {
        return r -> {
            final Thread thread = Executors.defaultThreadFactory().newThread(r);
            if (nameFormat != null) {
                thread.setName(String.format(Locale.ROOT, nameFormat, COUNT.incrementAndGet()));
            }
            thread.setDaemon(daemon);
            return thread;
        };
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
            environment.getMetricRegistry(), nameFormat);

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize, instrumentedThreadFactory, handler);
        executor.setRemoveOnCancelPolicy(removeOnCancel);

        environment.manage(new ExecutorServiceManager(executor, shutdownTime, nameFormat));
        return executor;
    }
}
