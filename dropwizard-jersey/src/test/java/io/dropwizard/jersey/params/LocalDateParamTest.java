package io.dropwizard.jersey.params;

import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateParamTest {
    @Test
    void parsesLocalDates() {
        final LocalDateParam param = new LocalDateParam("2012-11-20");

        assertThat(param.get())
                .isEqualTo(new LocalDate(2012, 11, 20));
    }
}
