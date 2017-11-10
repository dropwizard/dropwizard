package io.dropwizard.validation.selfvalidating;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelfValidatingValidatorTest {

    @SelfValidating
    public static class InvalidExample {

        @SelfValidation
        public void validateCorrect(ViolationCollector col) {
        }

        @SelfValidation
        public void validateFailAdditionalParameters(ViolationCollector col, int a) {
        }

        @SelfValidation
        public boolean validateFailReturn(ViolationCollector col) {
            return true;
        }

        @SelfValidation
        private void validateFailPrivate(ViolationCollector col) {
        }
    }

    private SelfValidatingValidator selfValidatingValidator = new SelfValidatingValidator();

    @Test
    public void correctMethod() throws Exception {
        assertThat(selfValidatingValidator.isCorrectMethod(InvalidExample.class
            .getDeclaredMethod("validateCorrect", ViolationCollector.class)))
            .isTrue();
    }

    @Test
    public void voidIsNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isCorrectMethod(InvalidExample.class
            .getDeclaredMethod("validateFailReturn", ViolationCollector.class)))
            .isFalse();
    }

    @Test
    public void privateIsNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isCorrectMethod(InvalidExample.class
            .getDeclaredMethod("validateFailPrivate", ViolationCollector.class)))
            .isFalse();
    }

    @Test
    public void additionalParametersAreNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isCorrectMethod(InvalidExample.class
            .getDeclaredMethod("validateFailAdditionalParameters", ViolationCollector.class, int.class)))
            .isFalse();
    }

}
