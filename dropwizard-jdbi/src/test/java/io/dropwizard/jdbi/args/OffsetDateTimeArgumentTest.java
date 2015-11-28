package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class OffsetDateTimeArgumentTest {

    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);
    private final StatementContext context = Mockito.mock(StatementContext.class);

    @Test
    public void apply() throws Exception {
        final ZoneOffset localOffset = ZoneOffset.from(OffsetDateTime.now());
        OffsetDateTime dateTime = OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, localOffset);

        new OffsetDateTimeArgument(dateTime, Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setTimestamp(1, Timestamp.valueOf("2007-12-03 10:15:30.375"));
    }

    @Test
    public void apply_ValueIsNull() throws Exception {
        new OffsetDateTimeArgument(null, Optional.empty()).apply(1, statement, context);

        Mockito.verify(statement).setNull(1, Types.TIMESTAMP);
    }
}
