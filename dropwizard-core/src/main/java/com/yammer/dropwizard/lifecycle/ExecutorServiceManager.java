package com.yammer.dropwizard.lifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceManager implements Managed {
    private final ExecutorService executor;
    private final long shutdownPeriod;
    private final TimeUnit unit;
    private final String poolName;

    public ExecutorServiceManager(ExecutorService executor, long shutdownPeriod, TimeUnit unit, String poolName) {
        this.executor = executor;
        this.shutdownPeriod = shutdownPeriod;
        this.unit = unit;
        this.poolName = poolName;
    }

    @Override
    public void start() throws Exception {
        // OK BOSS
    }

    @Override
    public void stop() throws Exception {
        executor.shutdown();
        executor.awaitTermination(shutdownPeriod, unit);
    }

    @Override
    public String toString() {
        return super.toString() + '(' + poolName + ')';
    }

}
