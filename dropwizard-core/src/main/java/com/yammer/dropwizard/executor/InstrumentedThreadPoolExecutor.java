package com.yammer.dropwizard.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * Instruments a thread pool executor to record execution metrics.
 */
public class InstrumentedThreadPoolExecutor extends ThreadPoolExecutor {

    private final Timer taskTimer;

    private final ThreadLocal<TimerContext> timerContext = new ThreadLocal<TimerContext>();

    public InstrumentedThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, String nameFormat) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(), threadFactory);

        taskTimer = Metrics.newTimer(ThreadPoolExecutor.class, "tasks-executed", nameFormat);

        Metrics.newGauge(ThreadPoolExecutor.class, "active-threads", nameFormat, new Gauge<Integer>() {
            @Override
            public Integer value() {
                return getActiveCount();
            }
        });
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        timerContext.get().stop();
        timerContext.remove();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        timerContext.set(taskTimer.time());
        super.beforeExecute(t, r);
    }

}
