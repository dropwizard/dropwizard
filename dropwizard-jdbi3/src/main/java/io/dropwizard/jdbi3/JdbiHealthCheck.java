package io.dropwizard.jdbi3;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.MoreExecutors;
import io.dropwizard.db.TimeBoundHealthCheck;
import io.dropwizard.util.Duration;
import org.jdbi.v3.core.Jdbi;

import java.util.concurrent.ExecutorService;

public class JdbiHealthCheck extends HealthCheck {
    private final Jdbi dbi;
    private final String validationQuery;
    private final TimeBoundHealthCheck timeBoundHealthCheck;

    public JdbiHealthCheck(ExecutorService executorService, Duration duration, Jdbi dbi, String validationQuery) {
        this.dbi = dbi;
        this.validationQuery = validationQuery;
        this.timeBoundHealthCheck = new TimeBoundHealthCheck(executorService, duration);
    }

    public JdbiHealthCheck(Jdbi dbi, String validationQuery) {
        this(MoreExecutors.newDirectExecutorService(), Duration.seconds(0), dbi, validationQuery);
    }

    @Override
    protected Result check() throws Exception {
        return timeBoundHealthCheck.check(() ->
            dbi.withHandle((handle) -> {
                handle.execute(validationQuery);
                return Result.healthy();
            })
        );
    }
}
