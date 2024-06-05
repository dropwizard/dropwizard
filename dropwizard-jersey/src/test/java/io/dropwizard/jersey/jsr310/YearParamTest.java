package io.dropwizard.jersey.jsr310;

import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

class YearParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final YearParam param = new YearParam("2012");

        assertThat(param)
            .extracting(YearParam::get)
            .isEqualTo(Year.of(2012));
    }
}
