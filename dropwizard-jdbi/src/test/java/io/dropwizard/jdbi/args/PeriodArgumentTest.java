package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.Period;

public class PeriodArgumentTest {

    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    private final StatementContext context = Mockito.mock(StatementContext.class);

    @Test
    public void apply() throws Exception {
        Period period = Period.parse("P12Y5M2D");

        new PeriodArgument(period).apply(1, statement, context);

        Mockito.verify(statement).setString(1, "P12Y5M2D");
    }

    @Test
    public void apply_ValueIsNull() throws Exception {
        new PeriodArgument(null).apply(1, statement, context);

        Mockito.verify(statement).setNull(1, Types.VARCHAR);
    }
}
