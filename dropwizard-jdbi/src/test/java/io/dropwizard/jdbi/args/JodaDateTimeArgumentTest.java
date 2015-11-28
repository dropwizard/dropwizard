package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;

public class JodaDateTimeArgumentTest {

    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    private final StatementContext context = Mockito.mock(StatementContext.class);

    @Test
    public void apply() throws Exception {
        DateTime dateTime = DateTime.parse("2007-12-03T10:15:30.375");

        new JodaDateTimeArgument(dateTime, Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setTimestamp(1, Timestamp.valueOf("2007-12-03 10:15:30.375"));
    }

    @Test
    public void apply_ValueIsNull() throws Exception {
        new JodaDateTimeArgument(null, Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setNull(1, Types.TIMESTAMP);
    }
}
