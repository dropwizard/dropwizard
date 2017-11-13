package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalDateMapperTest {

    private final ResultSet resultSet = mock(ResultSet.class);
    private final StatementContext ctx = mock(StatementContext.class);

    @Test
    public void mapColumnByName() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(Timestamp.valueOf("2007-12-03 00:00:00.000"));

        LocalDate actual = new LocalDateMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isEqualTo(LocalDate.parse("2007-12-03"));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(null);

        LocalDate actual = new LocalDateMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.valueOf("2007-12-03 00:00:00.000"));

        LocalDate actual = new LocalDateMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isEqualTo(LocalDate.parse("2007-12-03"));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        LocalDate actual = new LocalDateMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isNull();
    }
}
