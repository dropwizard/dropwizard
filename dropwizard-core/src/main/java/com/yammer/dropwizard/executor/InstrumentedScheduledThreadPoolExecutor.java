package com.yammer.dropwizard.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * Instruments a scheduled thread pool executor to record execution metrics.
 */
public class InstrumentedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    private final Timer taskTimer;

    private final ThreadLocal<TimerContext> timerContext = new ThreadLocal<TimerContext>();

    public InstrumentedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, String nameFormat) {
        super(corePoolSize, threadFactory);

        taskTimer = Metrics.newTimer(ScheduledThreadPoolExecutor.class, "tasks-executed", nameFormat);

        Metrics.newGauge(ScheduledThreadPoolExecutor.class, "active-threads", nameFormat, new Gauge<Integer>() {
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
