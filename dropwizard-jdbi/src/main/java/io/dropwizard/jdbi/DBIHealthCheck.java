package io.dropwizard.jdbi;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.db.TimeBoundHealthCheck;
import io.dropwizard.util.DirectExecutorService;
import io.dropwizard.util.Duration;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class DBIHealthCheck extends HealthCheck {
    private final DBI dbi;
    private final Optional<String> validationQuery;
    private final int validationQueryTimeout;
    private final TimeBoundHealthCheck timeBoundHealthCheck;

    public DBIHealthCheck(ExecutorService executorService, Duration duration, DBI dbi, Optional<String> validationQuery) {
        this.dbi = dbi;
        this.validationQuery = validationQuery;
        this.validationQueryTimeout = (int) duration.toSeconds();
        this.timeBoundHealthCheck = new TimeBoundHealthCheck(executorService, duration);
    }

    public DBIHealthCheck(DBI dbi, Optional<String> validationQuery) {
        this(new DirectExecutorService(), Duration.seconds(0), dbi, validationQuery);
    }

    @Override
    protected Result check() throws Exception {
        return timeBoundHealthCheck.check(() -> {
            try (Handle handle = dbi.open()) {
                if (validationQuery.isPresent()) {
                    handle.execute(validationQuery.get());
                } else if (!handle.getConnection().isValid(validationQueryTimeout)) {
                    return Result.unhealthy("Connection::isValid returned false.");
                }

                return Result.healthy();
            }
        });
    }

}
