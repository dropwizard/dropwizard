package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;

public class LocalDateArgumentTest {

    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    private final StatementContext context = Mockito.mock(StatementContext.class);

    @Test
    public void apply() throws Exception {
        LocalDate localDate = LocalDate.parse("2007-12-03");

        new LocalDateArgument(localDate).apply(1, statement, context);

        Mockito.verify(statement).setTimestamp(1, Timestamp.valueOf("2007-12-03 00:00:00.000"));
    }

    @Test
    public void apply_ValueIsNull() throws Exception {
        new LocalDateArgument(null).apply(1, statement, context);

        Mockito.verify(statement).setNull(1, Types.TIMESTAMP);
    }
}
