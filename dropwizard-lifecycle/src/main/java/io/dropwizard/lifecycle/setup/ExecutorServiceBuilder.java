package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutorServiceBuilder {
    private static Logger log = LoggerFactory.getLogger(ExecutorServiceBuilder.class);

    private static final AtomicLong COUNT = new AtomicLong(0);
    private final LifecycleEnvironment environment;
    @NonNull
    private final String nameFormat;
    private int corePoolSize;
    private int maximumPoolSize;
    private boolean allowCoreThreadTimeOut;
    private Duration keepAliveTime;
    private Duration shutdownTime;
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler handler;

    public ExecutorServiceBuilder(LifecycleEnvironment environment, String nameFormat, ThreadFactory factory) {
        this.environment = environment;
        this.nameFormat = nameFormat;
        this.corePoolSize = 0;
        this.maximumPoolSize = 1;
        this.allowCoreThreadTimeOut = false;
        this.keepAliveTime = Duration.seconds(60);
        this.shutdownTime = Duration.seconds(5);
        this.workQueue = new LinkedBlockingQueue<>();
        this.threadFactory = factory;
        this.handler = new ThreadPoolExecutor.AbortPolicy();
    }

    public ExecutorServiceBuilder(LifecycleEnvironment environment, String nameFormat) {
        this(environment, nameFormat, buildThreadFactory(nameFormat));
    }

    private static ThreadFactory buildThreadFactory(String nameFormat) {
        ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        // Validate the format string
        try (Formatter fmt = new Formatter()) {
            fmt.format(Locale.ROOT, nameFormat, 0);
        }

        return r -> {
            final Thread thread = defaultThreadFactory.newThread(r);
            thread.setName(String.format(Locale.ROOT, nameFormat, COUNT.incrementAndGet()));
            return thread;
        };
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

        final String nameWithoutFormat = getNameWithoutFormat(nameFormat);
        final ThreadFactory instrumentedThreadFactory = new InstrumentedThreadFactory(threadFactory, environment.getMetricRegistry(), nameWithoutFormat);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                                                                   maximumPoolSize,
                                                                   keepAliveTime.getQuantity(),
                                                                   keepAliveTime.getUnit(),
                                                                   workQueue,
                                                                   instrumentedThreadFactory,
                                                                   handler);
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        environment.manage(new ExecutorServiceManager(executor, shutdownTime, nameFormat));
        return executor;
    }

    static String getNameWithoutFormat(String nameFormat) {
        final String name = String.format(Locale.ROOT, nameFormat, 0);
        return commonPrefixWithoutHyphen(name, nameFormat) + commonSuffix(name, nameFormat);
    }

    static String commonPrefixWithoutHyphen(String name, String nameFormat) {
        final int minLength = Math.min(name.length(), nameFormat.length());
        int diffIndex;
        for (diffIndex = 0; diffIndex < minLength; diffIndex++) {
            if (name.charAt(diffIndex) != nameFormat.charAt(diffIndex)) {
                break;
            }
        }
        if (diffIndex > 0 && name.charAt(diffIndex - 1) == '-') {
            diffIndex--;
        }
        return name.substring(0, diffIndex);
    }

    static String commonSuffix(String name, String nameFormat) {
        int nameIndex = name.length();
        int nameFormatIndex = nameFormat.length();
        while (--nameIndex >= 0 && --nameFormatIndex >= 0) {
            if (name.charAt(nameIndex) != nameFormat.charAt(nameFormatIndex)) {
                break;
            }
        }
        return name.substring(nameIndex + 1);
    }

    private boolean isBoundedQueue() {
        return workQueue.remainingCapacity() != Integer.MAX_VALUE;
    }

    static synchronized void setLog(Logger newLog) {
        log = newLog;
    }
}
