package io.dropwizard.jersey.jsr310;

import org.junit.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalTimeParamTest {
    @Test
    public void parsesDateTimes() throws Exception {
        final LocalTimeParam param = new LocalTimeParam("12:34:56");

        assertThat(param.get())
                .isEqualTo(LocalTime.of(12, 34, 56));
    }
}
