package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.JettyManaged;
import com.codahale.dropwizard.lifecycle.ExecutorServiceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.*;

public class ExecutorServiceBuilder {
    private final List<Object> managedObjects;
    private final String nameFormat;
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private TimeUnit keepAliveUnit;
    private long shutdownTime;
    private TimeUnit shutdownUnit;
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler handler;

    public ExecutorServiceBuilder(List<Object> managedObjects, String nameFormat) {
        this.managedObjects = managedObjects;
        this.nameFormat = nameFormat;
        this.corePoolSize = 0;
        this.maximumPoolSize = Integer.MAX_VALUE;
        this.keepAliveTime = 60;
        this.keepAliveUnit = TimeUnit.SECONDS;
        this.shutdownTime = 5;
        this.shutdownUnit = TimeUnit.SECONDS;
        this.workQueue = new LinkedBlockingQueue<>();
        this.threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        this.handler = new ThreadPoolExecutor.AbortPolicy();
    }

    public ExecutorServiceBuilder minThreads(int threads) {
        this.corePoolSize = threads;
        return this;
    }

    public ExecutorServiceBuilder maxThreads(int threads) {
        this.maximumPoolSize = threads;
        return this;
    }

    public ExecutorServiceBuilder keepAliveTime(long time, TimeUnit unit) {
        this.keepAliveTime = time;
        this.keepAliveUnit = unit;
        return this;
    }

    public ExecutorServiceBuilder shutdownTime(long time, TimeUnit unit) {
        this.shutdownTime = time;
        this.shutdownUnit = unit;
        return this;
    }

    public ExecutorServiceBuilder workQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    public ExecutorServiceBuilder rejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    public ExecutorServiceBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public ExecutorService build() {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                                                                   maximumPoolSize,
                                                                   keepAliveTime,
                                                                   keepAliveUnit,
                                                                   workQueue,
                                                                   threadFactory,
                                                                   handler);
        managedObjects.add(new JettyManaged(new ExecutorServiceManager(executor,
                                                                       shutdownTime,
                                                                       shutdownUnit,
                                                                       nameFormat)));
        return executor;
    }
}
