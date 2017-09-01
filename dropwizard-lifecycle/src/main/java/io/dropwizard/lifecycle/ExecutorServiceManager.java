package io.dropwizard.lifecycle;

import io.dropwizard.util.Duration;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class ExecutorServiceManager implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceManager.class);
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

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException
     *             This is thrown if the thread executing this method is
     *             interrupted while awaiting executor tasks to complete.
     */
    @Override
    public void stop() throws InterruptedException, Exception {
        executor.shutdown();
        final boolean success = executor.awaitTermination(shutdownPeriod.getQuantity(), shutdownPeriod.getUnit());
        if (!success && LOG.isDebugEnabled()) {
            LOG.debug("Timeout has elapsed before termination completed for executor " + executor.toString());
        }
    }

    @Override
    public String toString() {
        return super.toString() + '(' + poolName + ')';
    }

    @VisibleForTesting
    public ExecutorService getExecutor() {
        return executor;
    }

    @VisibleForTesting
    public Duration getShutdownPeriod() {
        return shutdownPeriod;
    }

    @VisibleForTesting
    public String getPoolName() {
        return poolName;
    }
}
