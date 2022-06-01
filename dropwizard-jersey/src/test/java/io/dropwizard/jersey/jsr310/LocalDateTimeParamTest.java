package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LocalDateTimeParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final LocalDateTimeParam param = new LocalDateTimeParam("2012-11-19T13:37");

        assertThat(param.get()).isEqualTo(LocalDateTime.of(2012, 11, 19, 13, 37));
    }
}
