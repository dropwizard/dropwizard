package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorServiceBuilder {
    private static Logger log = LoggerFactory.getLogger(ExecutorServiceBuilder.class);

    private final LifecycleEnvironment environment;
    @Nonnull
    private final String namePrefix;
    private int corePoolSize;
    private int maximumPoolSize;
    private boolean allowCoreThreadTimeOut;
    private Duration keepAliveTime;
    private Duration shutdownTime;
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler handler;

    public ExecutorServiceBuilder(LifecycleEnvironment environment, String namePrefix, ThreadFactory factory) {
        this.environment = environment;
        this.namePrefix = namePrefix;
        this.corePoolSize = 0;
        this.maximumPoolSize = 1;
        this.allowCoreThreadTimeOut = false;
        this.keepAliveTime = Duration.seconds(60);
        this.shutdownTime = Duration.seconds(5);
        this.workQueue = new LinkedBlockingQueue<>();
        this.threadFactory = factory;
        this.handler = new ThreadPoolExecutor.AbortPolicy();
    }

    public ExecutorServiceBuilder(LifecycleEnvironment environment, String namePrefix) {
        this(environment, namePrefix, new NamedThreadFactory(namePrefix));
    }

    public ExecutorServiceBuilder minThreads(int threads) {
        this.corePoolSize = threads;
        return this;
    }

    public ExecutorServiceBuilder maxThreads(int threads) {
        this.maximumPoolSize = threads;
        return this;
    }

    public ExecutorServiceBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
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

        final ThreadFactory instrumentedThreadFactory = new InstrumentedThreadFactory(threadFactory, environment.getMetricRegistry(), namePrefix);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                                                                   maximumPoolSize,
                                                                   keepAliveTime.getQuantity(),
                                                                   keepAliveTime.getUnit(),
                                                                   workQueue,
                                                                   instrumentedThreadFactory,
                                                                   handler);
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        environment.manage(new ExecutorServiceManager(executor, shutdownTime, namePrefix));
        return executor;
    }

    private boolean isBoundedQueue() {
        return workQueue.remainingCapacity() != Integer.MAX_VALUE;
    }

    static synchronized void setLog(Logger newLog) {
        log = newLog;
    }
}
