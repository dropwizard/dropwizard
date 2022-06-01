package io.dropwizard.jdbi3.jersey;

import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import java.sql.SQLException;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterates through {@link SQLException}s to log all causes
 */
@Provider
public class LoggingSQLExceptionMapper extends LoggingExceptionMapper<SQLException> {
    /**
     * @since 2.0
     */
    LoggingSQLExceptionMapper(Logger logger) {
        super(logger);
    }

    /**
     * @since 2.0
     */
    public LoggingSQLExceptionMapper() {
        this(LoggerFactory.getLogger(LoggingSQLExceptionMapper.class));
    }

    @Override
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    protected void logException(long id, SQLException exception) {
        final String message = formatLogMessage(id, exception);
        for (Throwable throwable : exception) {
            logger.error(message, throwable);
        }
    }
}
