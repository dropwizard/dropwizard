package io.dropwizard.jersey.jsr310;

import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class OffsetDateTimeParamTest {
    @Test
    public void parsesDateTimes() throws Exception {
        final OffsetDateTimeParam param = new OffsetDateTimeParam("2012-11-19T13:37+01:00");

        assertThat(param.get())
                .isEqualTo(OffsetDateTime.of(2012, 11, 19, 13, 37, 0, 0, ZoneOffset.ofHours(1)));
    }
}
