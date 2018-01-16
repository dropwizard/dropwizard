package io.dropwizard.jdbi3.jersey;

import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.result.NoResultsException;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.transaction.TransactionException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LoggingDBIExceptionMapperTest {

    LoggingDBIExceptionMapper dbiExceptionMapper;
    Logger logger;

    @Before
    public void setUp() throws Exception {
        logger = mock(Logger.class);
        dbiExceptionMapper = new LoggingDBIExceptionMapper();
        LoggingDBIExceptionMapper.setLogger(logger);
    }

    @Test
    public void testSqlExceptionIsCause() throws Exception {
        StatementContext statementContext = mock(StatementContext.class);
        RuntimeException runtimeException = new RuntimeException("DB is down");
        SQLException sqlException = new SQLException("DB error", runtimeException);
        JdbiException dbiException = new NoResultsException("Unable get a result set", sqlException, statementContext);

        dbiExceptionMapper.logException(9812, dbiException);

        verify(logger).error("Error handling a request: 0000000000002654", sqlException);
        verify(logger).error("Error handling a request: 0000000000002654", runtimeException);
        verify(logger, never()).error("Error handling a request: 0000000000002654", dbiException);
    }

    @Test
    public void testPlainDBIException() throws Exception {
        JdbiException dbiException = new TransactionException("Transaction failed for unknown reason");

        dbiExceptionMapper.logException(9812, dbiException);

        verify(logger).error("Error handling a request: 0000000000002654", dbiException);
    }
}
