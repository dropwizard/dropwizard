package io.dropwizard.jersey.params;

import org.joda.time.LocalDate;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateParamTest {
    @Test
    public void parsesLocalDates() throws Exception {
        final LocalDateParam param = new LocalDateParam("2012-11-20");

        assertThat(param.get())
                .isEqualTo(new LocalDate(2012, 11, 20));
    }
}
