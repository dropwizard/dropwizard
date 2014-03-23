package io.dropwizard.jersey.params;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeParamTest {
    @Test
    public void parsesDateTimes() throws Exception {
        final DateTimeParam param = new DateTimeParam("2012-11-19");

        assertThat(param.get())
                .isEqualTo(new DateTime(2012, 11, 19, 0, 0, DateTimeZone.UTC));
    }
}
