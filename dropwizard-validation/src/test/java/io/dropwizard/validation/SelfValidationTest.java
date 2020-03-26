package io.dropwizard.validation;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.validation.selfvalidating.SelfValidating;
import io.dropwizard.validation.selfvalidating.SelfValidation;
import io.dropwizard.validation.selfvalidating.ViolationCollector;
import org.junit.Test;

import javax.validation.Validator;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SelfValidationTest {
    private static final String FAILED = "failed";

    @SelfValidating
    public static class FailingExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail(ViolationCollector col) {
            col.addViolation(FAILED);
        }
    }

    @SelfValidating
    public static class DirectContextExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail(ViolationCollector col) {
            col.getContext().buildConstraintViolationWithTemplate(FAILED).addConstraintViolation();
            col.setViolationOccurred(true);
        }
    }

    @SelfValidating
    public static class CorrectExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateCorrect(ViolationCollector col) {
        }
    }

    @SelfValidating
    public static class InvalidExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateCorrect(ViolationCollector col) {
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFailAdditionalParameters(ViolationCollector col, int a) {
            col.addViolation(FAILED);
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public boolean validateFailReturn(ViolationCollector col) {
            col.addViolation(FAILED);
            return true;
        }

        @SuppressWarnings("unused")
        @SelfValidation
        private void validateFailPrivate(ViolationCollector col) {
            col.addViolation(FAILED);
        }
    }

    @SelfValidating
    public static class ComplexExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail1(ViolationCollector col) {
            col.addViolation(FAILED + "1");
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail2(ViolationCollector col) {
            col.addViolation("p2", FAILED);
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail3(ViolationCollector col) {
            col.addViolation("p", 3, FAILED);
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail4(ViolationCollector col) {
            col.addViolation("p", "four", FAILED);
        }

        @SuppressWarnings("unused")
        @SelfValidation
        public void validateCorrect(ViolationCollector col) {
        }
    }

    @SelfValidating
    public static class NoValidations {
    }

    @SelfValidating
    public static class InjectionExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail(ViolationCollector col) {
            col.addViolation("${'value'}");
            col.addViolation("$\\A{1+1}");
            col.addViolation("{value}", Collections.singletonMap("value", "TEST"));
            col.addViolation("${'property'}", "${'value'}");
            col.addViolation("${'property'}", 1, "${'value'}");
            col.addViolation("${'property'}", "${'key'}", "${'value'}");
        }
    }

    @SelfValidating(escapeExpressions = false)
    public static class EscapingDisabledExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail(ViolationCollector col) {
            col.addViolation("${'value'}");
            col.addViolation("$\\A{1+1}");
            col.addViolation("{value}", Collections.singletonMap("value", "TEST"));
            col.addViolation("${'property'}", "${'value'}");
            col.addViolation("${'property'}", 1, "${'value'}");
            col.addViolation("${'property'}", "${'key'}", "${'value'}");
        }
    }

    @SelfValidating(escapeExpressions = false)
    public static class MessageParametersExample {
        @SuppressWarnings("unused")
        @SelfValidation
        public void validateFail(ViolationCollector col) {
            col.addViolation("{1+1}");
            col.addViolation("{value}", Collections.singletonMap("value", "VALUE"));
            col.addViolation("No parameter", Collections.singletonMap("value", "VALUE"));
            col.addViolation("{value} {unsetParameter}", Collections.singletonMap("value", "VALUE"));
            col.addViolation("{value", Collections.singletonMap("value", "VALUE"));
            col.addViolation("value}", Collections.singletonMap("value", "VALUE"));
            col.addViolation("{  value  }", Collections.singletonMap("value", "VALUE"));
            col.addViolation("Mixed ${'value'} {value}", Collections.singletonMap("value", "VALUE"));
            col.addViolation("Nested {value}", Collections.singletonMap("value", "${'nested'}"));
            col.addViolation("{property}", "{value}", ImmutableMap.of("property", "PROPERTY", "value", "VALUE"));
            col.addViolation("{property}", 1, "{value}", ImmutableMap.of("property", "PROPERTY", "value", "VALUE"));
            col.addViolation("{property}", "{key}", "{value}", ImmutableMap.of("property", "PROPERTY", "key", "KEY", "value", "VALUE"));
        }
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void failingExample() {
        assertThat(ConstraintViolations.format(validator.validate(new FailingExample())))
            .containsOnly(" " + FAILED);
    }

    @Test
    public void correctExample() {
        assertThat(ConstraintViolations.format(validator.validate(new CorrectExample())))
            .isEmpty();
    }

    @Test
    public void multipleTestingOfSameClass() {
        assertThat(ConstraintViolations.format(validator.validate(new CorrectExample())))
                .isEmpty();
        assertThat(ConstraintViolations.format(validator.validate(new CorrectExample())))
            .isEmpty();
    }

    @Test
    public void testDirectContextUsage() {
        assertThat(ConstraintViolations.format(validator.validate(new DirectContextExample())))
            .containsOnly(" " + FAILED);
    }

    @Test
    public void complexExample() {
        assertThat(ConstraintViolations.format(validator.validate(new ComplexExample())))
                .containsExactly(
                        " failed1",
                        "p2 failed",
                        "p[3] failed",
                        "p[four] failed");
    }

    @Test
    public void invalidExample() throws Exception {
        assertThat(ConstraintViolations.format(validator.validate(new InvalidExample())))
            .isEmpty();
    }

    @Test
    public void giveWarningIfNoValidationMethods() {
        assertThat(ConstraintViolations.format(validator.validate(new NoValidations())))
            .isEmpty();
    }

    @Test
    public void violationMessagesAreEscapedByDefault() {
        assertThat(ConstraintViolations.format(validator.validate(new InjectionExample()))).containsExactly(
                " $\\A{1+1}",
                " ${'value'}",
                " {value}",
                "${'property'} ${'value'}",
                "${'property'}[${'key'}] ${'value'}",
                "${'property'}[1] ${'value'}"
        );
    }

    @Test
    public void violationMessagesAreInterpolatedIfEscapingDisabled() {
        assertThat(ConstraintViolations.format(validator.validate(new EscapingDisabledExample()))).containsExactly(
                " A2",
                " TEST",
                " value",
                "${'property'} value",
                "${'property'}[${'key'}] value",
                "${'property'}[1] value"
        );
    }

    @Test
    public void messageParametersExample() {
        assertThat(ConstraintViolations.format(validator.validate(new MessageParametersExample()))).containsExactly(
                " Mixed value VALUE",
                " Nested ${'nested'}",
                " No parameter",
                " VALUE",
                " VALUE {unsetParameter}",
                " value}",
                " {  value  }",
                " {1+1}",
                " {value",
                "{property} VALUE",
                "{property}[1] VALUE",
                "{property}[{key}] VALUE"
        );
    }
}
