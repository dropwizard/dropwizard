package io.dropwizard.validation;

import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;
import org.junit.Test;

import javax.validation.Valid;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
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

        @Valid
        private List<@MaxSize(value = 30, unit = SizeUnit.KILOBYTES) Size> maxSize =
                Collections.singletonList(Size.gigabytes(2));

        @Valid
        private List<@MinSize(value = 30, unit = SizeUnit.KILOBYTES) Size> minSize =
                Collections.singletonList(Size.bytes(100));

        @Valid
        private List<@SizeRange(min = 10, max = 100, unit = SizeUnit.KILOBYTES) Size> rangeSize =
                Collections.singletonList(Size.megabytes(2));

        public void setTooBig(Size tooBig) {
            this.tooBig = tooBig;
        }
        public void setTooSmall(Size tooSmall) {
            this.tooSmall = tooSmall;
        }
        public void setOutOfRange(Size outOfRange) {
            this.outOfRange = outOfRange;
        }
        public void setMaxSize(List<Size> maxSize) {
            this.maxSize = maxSize;
        }
        public void setMinSize(List<Size> minSize) {
            this.minSize = minSize;
        }
        public void setRangeSize(List<Size> rangeSize) {
            this.rangeSize = rangeSize;
        }
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(ConstraintViolations.format(validator.validate(new Example())))
                    .containsOnly("outOfRange must be between 10 KILOBYTES and 100 KILOBYTES",
                                  "tooBig must be less than or equal to 30 KILOBYTES",
                                  "tooSmall must be greater than or equal to 30 KILOBYTES",
                                   "maxSize[0].<collection element> must be less than or equal to 30 KILOBYTES",
                                   "minSize[0].<collection element> must be greater than or equal to 30 KILOBYTES",
                                   "rangeSize[0].<collection element> must be between 10 KILOBYTES and 100 KILOBYTES");
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setTooBig(Size.bytes(10));
        example.setTooSmall(Size.megabytes(10));
        example.setOutOfRange(Size.kilobytes(64));
        example.setMaxSize(Collections.singletonList(Size.bytes(10)));
        example.setMinSize(Collections.singletonList(Size.megabytes(10)));
        example.setRangeSize(Collections.singletonList(Size.kilobytes(64)));

        assertThat(validator.validate(example))
                .isEmpty();
    }
}
