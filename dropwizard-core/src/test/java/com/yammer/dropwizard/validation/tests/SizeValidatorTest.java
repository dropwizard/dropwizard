package com.yammer.dropwizard.validation.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.util.SizeUnit;
import com.yammer.dropwizard.validation.*;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @MaxSize(value = 30, unit = SizeUnit.KILOBYTES)
        private Size tooBig = Size.gigabytes(2);

        @MinSize(value = 30, unit = SizeUnit.KILOBYTES)
        private Size tooSmall = Size.bytes(100);

        public void setTooBig(Size tooBig) {
            this.tooBig = tooBig;
        }
        public void setTooSmall(Size tooSmall) {
            this.tooSmall = tooSmall;
        }
    }

    private final Validator validator = new Validator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(validator.validate(new Example()),
                    is(ImmutableList.of(
                            "tooBig must be less than or equal to 30 KILOBYTES (was 2 gigabytes)",
                            "tooSmall must be greater than or equal to 30 KILOBYTES (was 100 bytes)")));
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setTooBig(Size.bytes(10));
        example.setTooSmall(Size.megabytes(10));

        assertThat(validator.validate(example),
                is(ImmutableList.<String>of()));
    }
}
