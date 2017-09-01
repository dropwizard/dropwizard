package io.dropwizard.lifecycle.setup;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;

public class ScheduledExecutorServiceBuilder {

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
        this(environment, nameFormat, new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(useDaemonThreads).build());
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
        this.removeOnCancel = Boolean.valueOf(removeOnCancel);
        return this;
    }

    public ScheduledExecutorService build() {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(this.poolSize, this.threadFactory, this.handler);
        executor.setRemoveOnCancelPolicy(this.removeOnCancel);

        this.environment.manage(new ExecutorServiceManager(executor, this.shutdownTime, this.nameFormat));
        return executor;
    }
}
