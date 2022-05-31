package io.dropwizard.validation.selfvalidating;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SelfValidatingValidatorTest {
    private final Logger log = mock(Logger.class);
    private final SelfValidatingValidator selfValidatingValidator = new SelfValidatingValidator(log);

    @Test
    void validObjectHasNoViolations() {
        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<ValidExample>> violations = validator.validate(new ValidExample(1));
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidObjectHasViolations() {
        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<ValidExample>> violations = validator.validate(new ValidExample(-1));
        assertThat(violations)
                .singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("n must be positive!");
    }

    @Test
    void correctMethod() {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateCorrect", ViolationCollector.class)))
                .isTrue();
    }

    @Test
    void voidIsNotAccepted() {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailReturn", ViolationCollector.class)))
                .isFalse();
    }

    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void privateIsNotAccepted() throws NoSuchMethodException {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailPrivate", ViolationCollector.class)))
                .isFalse();

        verify(log).error("The method {} is annotated with @SelfValidation but is not public",
            InvalidExample.class.getDeclaredMethod("validateFailPrivate", ViolationCollector.class));
    }

    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void additionalParametersAreNotAccepted() throws NoSuchMethodException {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailAdditionalParameters", ViolationCollector.class, int.class)))
                .isFalse();

        verify(log).error("The method {} is annotated with @SelfValidation but does not have a single parameter of type {}",
            InvalidExample.class.getMethod("validateFailAdditionalParameters", ViolationCollector.class, int.class),
            ViolationCollector.class);
    }

    private ResolvedMethod getMethod(String name, Class<?>... params) {
        AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_AND_INHERIT_IF_INHERITED);
        TypeResolver typeResolver = new TypeResolver();
        MemberResolver memberResolver = new MemberResolver(typeResolver);
        ResolvedTypeWithMembers annotatedType = memberResolver.resolve(typeResolver.resolve(InvalidExample.class), annotationConfiguration, null);
        for (ResolvedMethod m : annotatedType.getMemberMethods()) {
            if (hasSignature(m, name, params)) {
                return m;
            }
        }
        throw new IllegalStateException("Could not resolve method " + name + Arrays.toString(params) + " in " + InvalidExample.class);
    }

    private boolean hasSignature(ResolvedMethod m, String name, Class<?>[] params) {
        if (!m.getName().equals(name) || m.getArgumentCount() != params.length) {
            return false;
        }
        for (int i = 0; i < params.length; i++) {
            if (!m.getArgumentType(i).getErasedType().equals(params[i]))
                return false;
        }
        return true;
    }


    @SelfValidating
    static class InvalidExample {
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

    @SelfValidating
    static class ValidExample {
        final int n;

        ValidExample(int n) {
            this.n = n;
        }

        @SelfValidation
        public void validate(ViolationCollector col) {
            if (n < 0) {
                col.addViolation("n must be positive!");
            }
        }
    }
}
