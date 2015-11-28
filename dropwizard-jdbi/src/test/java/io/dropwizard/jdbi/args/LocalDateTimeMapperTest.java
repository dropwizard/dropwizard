package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LocalDateTimeMapperTest {

    private final ResultSet resultSet = Mockito.mock(ResultSet.class);

    @Test
    public void mapColumnByName() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        LocalDateTime actual = new LocalDateTimeMapper().mapColumn(resultSet, "name", null);

        assertThat(actual).isEqualTo(LocalDateTime.parse("2007-12-03T10:15:30.375"));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(null);

        LocalDateTime actual = new LocalDateTimeMapper().mapColumn(resultSet, "name", null);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        LocalDateTime actual = new LocalDateTimeMapper().mapColumn(resultSet, 1, null);

        assertThat(actual).isEqualTo(LocalDateTime.parse("2007-12-03T10:15:30.375"));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        LocalDateTime actual = new LocalDateTimeMapper().mapColumn(resultSet, 1, null);

        assertThat(actual).isNull();
    }
}
