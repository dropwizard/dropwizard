package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import org.junit.jupiter.api.Test;

class YearParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final YearParam param = new YearParam("2012");

        assertThat(param.get()).isEqualTo(Year.of(2012));
    }
}
