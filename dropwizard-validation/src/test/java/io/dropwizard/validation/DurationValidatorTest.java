package io.dropwizard.validation;

import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Duration;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class DurationValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @MaxDuration(value = 30, unit = TimeUnit.SECONDS)
        private Duration tooBig = Duration.minutes(10);

        @MinDuration(value = 30, unit = TimeUnit.SECONDS)
        private Duration tooSmall = Duration.milliseconds(100);
        
        @DurationRange(min = 10, max = 30, unit = TimeUnit.MINUTES)
        private Duration outOfRange = Duration.minutes(60);

        public void setTooBig(Duration tooBig) {
            this.tooBig = tooBig;
        }
        public void setTooSmall(Duration tooSmall) {
            this.tooSmall = tooSmall;
        }
        public void setOutOfRange(Duration outOfRange) {
            this.outOfRange = outOfRange;
        }
    }

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            final ImmutableList<String> errors =
                    ConstraintViolations.format(validator.validate(new Example()));

            assertThat(errors)
                    .containsOnly(
                            "outOfRange must be between 10 MINUTES and 30 MINUTES (was 60 minutes)",
                            "tooBig must be less than or equal to 30 SECONDS (was 10 minutes)",
                            "tooSmall must be greater than or equal to 30 SECONDS (was 100 milliseconds)");
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setTooBig(Duration.seconds(10));
        example.setTooSmall(Duration.seconds(100));
        example.setOutOfRange(Duration.minutes(15));

        assertThat(validator.validate(example))
                .isEmpty();
    }
}
