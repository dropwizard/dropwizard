package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

public class InstantArgumentTest {

    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    private final StatementContext context = Mockito.mock(StatementContext.class);

    @Test
    public void apply() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2012-12-21T00:00:00.000Z");
        ZonedDateTime expected = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());

        new InstantArgument(zonedDateTime.toInstant(), Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setTimestamp(1, Timestamp.from(expected.toInstant()));
    }

    @Test
    public void apply_ValueIsNull() throws Exception {
        new InstantArgument(null, Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setNull(1, Types.TIMESTAMP);
    }

    @Test
    public void applyCalendar() throws Exception {
        final ZoneId systemDefault = ZoneId.systemDefault();

        // this test only asserts that a calendar was passed in. Not that the JDBC driver
        // will do the right thing and adjust the time.
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2012-12-21T00:00:00.000Z");
        final ZonedDateTime expected = zonedDateTime.withZoneSameInstant(systemDefault);
        final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone(systemDefault));

        new InstantArgument(zonedDateTime.toInstant(), Optional.of(calendar)).apply(1, statement, context);

        Mockito.verify(statement).setTimestamp(1, Timestamp.from(expected.toInstant()), calendar);
    }
}
