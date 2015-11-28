package io.dropwizard.jersey.jsr310;

import org.junit.Test;

import java.time.Month;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

public class YearMonthParamTest {
    @Test
    public void parsesDateTimes() throws Exception {
        final YearMonthParam param = new YearMonthParam("2012-11");

        assertThat(param.get())
                .isEqualTo(YearMonth.of(2012, Month.NOVEMBER));
    }
}
