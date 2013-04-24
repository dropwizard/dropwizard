package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.JettyManaged;
import com.codahale.dropwizard.lifecycle.ExecutorServiceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.*;

public class ScheduledExecutorServiceBuilder {
    private final List<Object> managedObjects;
    private final String nameFormat;
    private int poolSize;
    private ThreadFactory threadFactory;
    private long shutdownTime;
    private TimeUnit shutdownUnit;
    private RejectedExecutionHandler handler;

    public ScheduledExecutorServiceBuilder(List<Object> managedObjects, String nameFormat) {
        this.managedObjects = managedObjects;
        this.nameFormat = nameFormat;
        this.poolSize = 1;
        this.threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        this.handler = new ThreadPoolExecutor.AbortPolicy();
    }

    public ScheduledExecutorServiceBuilder threads(int threads) {
        this.poolSize = threads;
        return this;
    }

    public ScheduledExecutorServiceBuilder shutdownTime(long time, TimeUnit unit) {
        this.shutdownTime = time;
        this.shutdownUnit = unit;
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

    public ScheduledExecutorService build() {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(poolSize,
                                                                                     threadFactory,
                                                                                     handler);
        managedObjects.add(new JettyManaged(new ExecutorServiceManager(executor,
                                                                       shutdownTime,
                                                                       shutdownUnit,
                                                                       nameFormat)));
        return executor;
    }
}
