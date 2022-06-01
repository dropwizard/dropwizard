package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LocalDateParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final LocalDateParam param = new LocalDateParam("2012-11-19");

        assertThat(param.get()).isEqualTo(LocalDate.of(2012, 11, 19));
    }
}
