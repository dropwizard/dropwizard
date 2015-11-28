package io.dropwizard.jdbi.jersey;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.exceptions.NoResultsException;
import org.skife.jdbi.v2.exceptions.TransactionFailedException;
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
        DBIException dbiException = new NoResultsException("Unable get a result set", sqlException, statementContext);

        dbiExceptionMapper.logException(9812, dbiException);

        verify(logger).error("Error handling a request: 0000000000002654", sqlException);
        verify(logger).error("Error handling a request: 0000000000002654", runtimeException);
        verify(logger, never()).error("Error handling a request: 0000000000002654", dbiException);
    }

    @Test
    public void testPlainDBIException() throws Exception {
        DBIException dbiException = new TransactionFailedException("Transaction failed for unknown reason");

        dbiExceptionMapper.logException(9812, dbiException);

        verify(logger).error("Error handling a request: 0000000000002654", dbiException);
    }
}
