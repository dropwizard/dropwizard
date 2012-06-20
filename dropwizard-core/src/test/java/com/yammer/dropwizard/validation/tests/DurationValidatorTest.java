package com.yammer.dropwizard.validation.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.validation.MaxDuration;
import com.yammer.dropwizard.validation.MinDuration;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DurationValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @MaxDuration(value = 30, unit = TimeUnit.SECONDS)
        private Duration tooBig = Duration.minutes(10);

        @MinDuration(value = 30, unit = TimeUnit.SECONDS)
        private Duration tooSmall = Duration.milliseconds(100);

        public void setTooBig(Duration tooBig) {
            this.tooBig = tooBig;
        }
        public void setTooSmall(Duration tooSmall) {
            this.tooSmall = tooSmall;
        }
    }

    private final Validator validator = new Validator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(validator.validate(new Example()),
                    is(ImmutableList.of(
                            "tooBig must be less than or equal to 30 SECONDS (was 10 minutes)",
                            "tooSmall must be greater than or equal to 30 SECONDS (was 100 milliseconds)")));
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setTooBig(Duration.seconds(10));
        example.setTooSmall(Duration.seconds(100));

        assertThat(validator.validate(example),
                is(ImmutableList.<String>of()));
    }
}
