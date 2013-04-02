package com.yammer.dropwizard.setup;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yammer.dropwizard.jetty.JettyManaged;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;
import org.eclipse.jetty.util.component.AggregateLifeCycle;

import java.util.concurrent.*;

public class ScheduledExecutorServiceBuilder {
    private final AggregateLifeCycle lifeCycle;
    private final String nameFormat;
    private int poolSize;
    private ThreadFactory threadFactory;
    private long shutdownTime;
    private TimeUnit shutdownUnit;
    private RejectedExecutionHandler handler;

    public ScheduledExecutorServiceBuilder(AggregateLifeCycle lifeCycle, String nameFormat) {
        this.lifeCycle = lifeCycle;
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
        lifeCycle.addBean(new JettyManaged(new ExecutorServiceManager(executor,
                                                                      shutdownTime,
                                                                      shutdownUnit,
                                                                      nameFormat)));
        return executor;
    }
}
