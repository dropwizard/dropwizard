package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.time.Period;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class PeriodMapperTest {

    private final ResultSet resultSet = Mockito.mock(ResultSet.class);

    @Test
    public void mapColumnByName() throws Exception {
        when(resultSet.getString("name")).thenReturn("P12Y5M2D");

        Period actual = new PeriodMapper().mapColumn(resultSet, "name", null);

        assertThat(actual).isEqualTo(Period.parse("P12Y5M2D"));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getString("name")).thenReturn(null);

        Period actual = new PeriodMapper().mapColumn(resultSet, "name", null);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        when(resultSet.getString(1)).thenReturn("P12Y5M2D");

        Period actual = new PeriodMapper().mapColumn(resultSet, 1, null);

        assertThat(actual).isEqualTo(Period.parse("P12Y5M2D"));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getString(1)).thenReturn(null);

        Period actual = new PeriodMapper().mapColumn(resultSet, 1, null);

        assertThat(actual).isNull();
    }
}
