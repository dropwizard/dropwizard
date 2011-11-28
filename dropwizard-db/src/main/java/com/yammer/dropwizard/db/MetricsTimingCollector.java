package com.yammer.dropwizard.db;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TimingCollector;

import java.util.concurrent.TimeUnit;

class MetricsTimingCollector implements TimingCollector {
    private final MetricsRegistry registry;
    private final TimerMetric defaultTimer;

    MetricsTimingCollector(MetricsRegistry registry) {
        this.registry = registry;
        this.defaultTimer = registry.newTimer(Database.class, "raw-sql");
    }

    @Override
    public void collect(long elapsedTime, StatementContext ctx) {
        final TimerMetric timer = getTimer(ctx);
        timer.update(elapsedTime, TimeUnit.NANOSECONDS);
    }

    private TimerMetric getTimer(StatementContext ctx) {
        if ((ctx.getSqlObjectType() == null) || (ctx.getSqlObjectMethod() == null)) {
            return defaultTimer;
        }

        return  registry.newTimer(ctx.getSqlObjectType(), ctx.getSqlObjectMethod().getName());
    }
}
