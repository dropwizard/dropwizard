package io.dropwizard.jdbi3.jersey;

import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.result.NoResultsException;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.transaction.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LoggingJdbiExceptionMapperTest {
    private LoggingJdbiExceptionMapper jdbiExceptionMapper;
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        logger = mock(Logger.class);
        jdbiExceptionMapper = new LoggingJdbiExceptionMapper(logger);
    }

    @Test
    void testSqlExceptionIsCause() throws Exception {
        StatementContext statementContext = mock(StatementContext.class);
        RuntimeException runtimeException = new RuntimeException("DB is down");
        SQLException sqlException = new SQLException("DB error", runtimeException);
        JdbiException jdbiException = new NoResultsException("Unable get a result set", sqlException, statementContext);

        jdbiExceptionMapper.logException(9812, jdbiException);

        verify(logger).error("Error handling a request: 0000000000002654", sqlException);
        verify(logger).error("Error handling a request: 0000000000002654", runtimeException);
        verify(logger, never()).error("Error handling a request: 0000000000002654", jdbiException);
    }

    @Test
    void testPlainJdbiException() {
        JdbiException jdbiException = new TransactionException("Transaction failed for unknown reason");

        jdbiExceptionMapper.logException(9812, jdbiException);

        verify(logger).error("Error handling a request: 0000000000002654", jdbiException);
    }
}
