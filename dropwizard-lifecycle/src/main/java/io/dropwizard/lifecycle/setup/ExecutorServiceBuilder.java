package io.dropwizard.lifecycle.setup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorServiceBuilder {
    private static Logger log = LoggerFactory.getLogger(ExecutorServiceBuilder.class);

    private final LifecycleEnvironment environment;
    private final String nameFormat;
    private int corePoolSize;
    private int maximumPoolSize;
    private Duration keepAliveTime;
    private Duration shutdownTime;
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler handler;

    public ExecutorServiceBuilder(LifecycleEnvironment environment, String nameFormat) {
        this.environment = environment;
        this.nameFormat = nameFormat;
        this.corePoolSize = 0;
        this.maximumPoolSize = 1;
        this.keepAliveTime = Duration.seconds(60);
        this.shutdownTime = Duration.seconds(5);
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

    public ExecutorServiceBuilder keepAliveTime(Duration time) {
        this.keepAliveTime = time;
        return this;
    }

    public ExecutorServiceBuilder shutdownTime(Duration time) {
        this.shutdownTime = time;
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
        if (corePoolSize != maximumPoolSize && maximumPoolSize > 1 && !isBoundedQueue()) {
            log.warn("Parameter 'maximumPoolSize' is conflicting with unbounded work queues");
        }
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                                                                   maximumPoolSize,
                                                                   keepAliveTime.getQuantity(),
                                                                   keepAliveTime.getUnit(),
                                                                   workQueue,
                                                                   threadFactory,
                                                                   handler);
        environment.manage(new ExecutorServiceManager(executor, shutdownTime, nameFormat));
        return executor;
    }

    private boolean isBoundedQueue() {
        return workQueue.remainingCapacity() != Integer.MAX_VALUE;
    }

    @VisibleForTesting
    static synchronized void setLog(Logger newLog) {
       log = newLog;
    }
}
