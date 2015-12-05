package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OffsetDateTimeMapperTest {

    private final ResultSet resultSet = Mockito.mock(ResultSet.class);

    @Test
    public void mapColumnByName() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, "name", null);

        final ZoneOffset localOffset = ZoneOffset.from(OffsetDateTime.now());
        assertThat(actual).isEqualTo(OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, localOffset));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(null);

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, "name", null);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.valueOf("2007-12-03 10:15:30.375"));

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, 1, null);

        final ZoneOffset localOffset = ZoneOffset.from(OffsetDateTime.now());
        assertThat(actual).isEqualTo(OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, localOffset));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, 1, null);

        assertThat(actual).isNull();
    }
}
