package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class LocalTimeParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final LocalTimeParam param = new LocalTimeParam("12:34:56");

        assertThat(param.get()).isEqualTo(LocalTime.of(12, 34, 56));
    }
}
