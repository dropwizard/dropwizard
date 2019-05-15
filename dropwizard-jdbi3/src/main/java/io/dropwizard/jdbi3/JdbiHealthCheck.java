package io.dropwizard.jdbi3;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.db.TimeBoundHealthCheck;
import io.dropwizard.util.DirectExecutorService;
import io.dropwizard.util.Duration;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class JdbiHealthCheck extends HealthCheck {
    private final Jdbi jdbi;
    private final Optional<String> validationQuery;
    private final int validationQueryTimeout;
    private final TimeBoundHealthCheck timeBoundHealthCheck;

    public JdbiHealthCheck(ExecutorService executorService, Duration duration, Jdbi jdbi, Optional<String> validationQuery) {
        this.jdbi = jdbi;
        this.validationQuery = validationQuery;
        this.validationQueryTimeout = (int) duration.toSeconds();
        this.timeBoundHealthCheck = new TimeBoundHealthCheck(executorService, duration);
    }

    public JdbiHealthCheck(Jdbi jdbi, Optional<String> validationQuery) {
        this(new DirectExecutorService(), Duration.seconds(0), jdbi, validationQuery);
    }

    @Override
    protected Result check() throws Exception {
        return timeBoundHealthCheck.check(() -> {
                try (Handle handle = jdbi.open()) {
                    if (validationQuery.isPresent()) {
                        handle.execute(validationQuery.get());
                    } else if (!handle.getConnection().isValid(validationQueryTimeout)) {
                        return Result.unhealthy("Connection::isValid returned false.");
                    }
                    return Result.healthy();
                }
            }
        );
    }
}
