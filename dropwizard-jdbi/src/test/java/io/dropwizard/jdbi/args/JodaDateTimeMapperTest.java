package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JodaDateTimeMapperTest {

    private final ResultSet resultSet = mock(ResultSet.class);
    private final StatementContext ctx = mock(StatementContext.class);

    @Test
    public void mapColumnByName() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        DateTime actual = new JodaDateTimeMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isEqualTo(DateTime.parse("2007-12-03T10:15:30.375"));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(null);

        DateTime actual = new JodaDateTimeMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        DateTime actual = new JodaDateTimeMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isEqualTo(DateTime.parse("2007-12-03T10:15:30.375"));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        DateTime actual = new JodaDateTimeMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isNull();
    }
}
