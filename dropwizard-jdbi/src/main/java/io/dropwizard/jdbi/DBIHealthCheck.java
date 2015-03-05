package io.dropwizard.jdbi;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.MoreExecutors;
import io.dropwizard.db.TimeBoundHealthChecks;
import io.dropwizard.util.Duration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class DBIHealthCheck extends HealthCheck {
    private final DBI dbi;
    private final String validationQuery;
    private final Duration duration;
    private final ExecutorService executorService;
    
    public DBIHealthCheck(ExecutorService executorService, Duration duration, DBI dbi, String validationQuery) {
        this.dbi = dbi;
        this.validationQuery = validationQuery;
        this.duration = duration;
        this.executorService = executorService;
    }
    
    public DBIHealthCheck(DBI dbi, String validationQuery) {
        this(MoreExecutors.newDirectExecutorService(), Duration.seconds(0), dbi, validationQuery);
    }

    @Override
    protected Result check() throws Exception {
        return TimeBoundHealthChecks.check(executorService, duration, new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try (Handle handle = dbi.open()) {
                    handle.execute(validationQuery);
                    return Result.healthy();
                }
            }
        });
    }

}
