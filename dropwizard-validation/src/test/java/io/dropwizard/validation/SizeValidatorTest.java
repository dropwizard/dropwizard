package io.dropwizard.validation;

import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;
import org.junit.Test;

import javax.validation.Validator;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class SizeValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @MaxSize(value = 30, unit = SizeUnit.KILOBYTES)
        private Size tooBig = Size.gigabytes(2);

        @MinSize(value = 30, unit = SizeUnit.KILOBYTES)
        private Size tooSmall = Size.bytes(100);

        @SizeRange(min = 10, max = 100, unit = SizeUnit.KILOBYTES)
        private Size outOfRange = Size.megabytes(2);

        public void setTooBig(Size tooBig) {
            this.tooBig = tooBig;
        }
        public void setTooSmall(Size tooSmall) {
            this.tooSmall = tooSmall;
        }
        public void setOutOfRange(Size outOfRange) {
            this.outOfRange = outOfRange;
        }
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(ConstraintViolations.format(validator.validate(new Example())))
                    .containsOnly("outOfRange must be between 10 KILOBYTES and 100 KILOBYTES",
                                  "tooBig must be less than or equal to 30 KILOBYTES",
                                  "tooSmall must be greater than or equal to 30 KILOBYTES");
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setTooBig(Size.bytes(10));
        example.setTooSmall(Size.megabytes(10));
        example.setOutOfRange(Size.kilobytes(64));

        assertThat(validator.validate(example))
                .isEmpty();
    }
}
