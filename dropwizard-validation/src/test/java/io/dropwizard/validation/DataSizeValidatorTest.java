package io.dropwizard.validation;

import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class DataSizeValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @MaxDataSize(value = 30, unit = DataSizeUnit.KILOBYTES)
        private DataSize tooBig = DataSize.gigabytes(2);

        @MinDataSize(value = 30, unit = DataSizeUnit.KILOBYTES)
        private DataSize tooSmall = DataSize.bytes(100);

        @DataSizeRange(min = 10, max = 100, unit = DataSizeUnit.KILOBYTES)
        private DataSize outOfRange = DataSize.megabytes(2);

        @Valid
        private List<@MaxDataSize(value = 30, unit = DataSizeUnit.KILOBYTES) DataSize> maxDataSize =
                Collections.singletonList(DataSize.gigabytes(2));

        @Valid
        private List<@MinDataSize(value = 30, unit = DataSizeUnit.KILOBYTES) DataSize> minDataSize =
                Collections.singletonList(DataSize.bytes(100));

        @Valid
        private List<@DataSizeRange(min = 10, max = 100, unit = DataSizeUnit.KILOBYTES) DataSize> rangeDataSize =
                Collections.singletonList(DataSize.megabytes(2));

        void setTooBig(DataSize tooBig) {
            this.tooBig = tooBig;
        }

        void setTooSmall(DataSize tooSmall) {
            this.tooSmall = tooSmall;
        }

        void setOutOfRange(DataSize outOfRange) {
            this.outOfRange = outOfRange;
        }

        void setMaxDataSize(List<DataSize> maxDataSize) {
            this.maxDataSize = maxDataSize;
        }

        void setMinDataSize(List<DataSize> minDataSize) {
            this.minDataSize = minDataSize;
        }

        void setRangeDataSize(List<DataSize> rangeDataSize) {
            this.rangeDataSize = rangeDataSize;
        }
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    void returnsASetOfErrorsForAnObject() {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(ConstraintViolations.format(validator.validate(new Example())))
                    .containsOnly("outOfRange must be between 10 KILOBYTES and 100 KILOBYTES",
                            "tooBig must be less than or equal to 30 KILOBYTES",
                            "tooSmall must be greater than or equal to 30 KILOBYTES",
                            "maxDataSize[0].<list element> must be less than or equal to 30 KILOBYTES",
                            "minDataSize[0].<list element> must be greater than or equal to 30 KILOBYTES",
                            "rangeDataSize[0].<list element> must be between 10 KILOBYTES and 100 KILOBYTES");
        }
    }

    @Test
    void returnsAnEmptySetForAValidObject() {
        final Example example = new Example();
        example.setTooBig(DataSize.bytes(10));
        example.setTooSmall(DataSize.megabytes(10));
        example.setOutOfRange(DataSize.kilobytes(64));
        example.setMaxDataSize(Collections.singletonList(DataSize.bytes(10)));
        example.setMinDataSize(Collections.singletonList(DataSize.megabytes(10)));
        example.setRangeDataSize(Collections.singletonList(DataSize.kilobytes(64)));

        assertThat(validator.validate(example)).isEmpty();
    }
}
