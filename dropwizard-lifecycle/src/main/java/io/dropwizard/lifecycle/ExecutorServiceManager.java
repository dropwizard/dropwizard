package io.dropwizard.lifecycle;

import io.dropwizard.util.Duration;

import java.util.concurrent.ExecutorService;

public class ExecutorServiceManager implements Managed {
    private final ExecutorService executor;
    private final Duration shutdownPeriod;
    private final String poolName;

    public ExecutorServiceManager(ExecutorService executor, Duration shutdownPeriod, String poolName) {
        this.executor = executor;
        this.shutdownPeriod = shutdownPeriod;
        this.poolName = poolName;
    }

    @Override
    public void start() throws Exception {
        // OK BOSS
    }

    @Override
    public void stop() throws Exception {
        executor.shutdown();
        executor.awaitTermination(shutdownPeriod.getQuantity(), shutdownPeriod.getUnit());
    }

    @Override
    public String toString() {
        return super.toString() + '(' + poolName + ')';
    }

}
