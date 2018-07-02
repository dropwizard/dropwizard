package io.dropwizard.validation.selfvalidating;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import io.dropwizard.validation.BaseValidator;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SelfValidatingValidatorTest {
    private SelfValidatingValidator selfValidatingValidator = new SelfValidatingValidator();

    @Test
    public void validObjectHasNoViolations() throws Exception {
        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<ValidExample>> violations = validator.validate(new ValidExample(1));
        assertThat(violations).isEmpty();
    }

    @Test
    public void invalidObjectHasViolations() throws Exception {
        final Validator validator = BaseValidator.newValidator();
        final Set<ConstraintViolation<ValidExample>> violations = validator.validate(new ValidExample(-1));
        assertThat(violations)
                .isNotEmpty()
                .allSatisfy(violation -> assertThat(violation.getMessage()).isEqualTo("n must be positive!"));
    }

    @Test
    public void correctMethod() throws Exception {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateCorrect", ViolationCollector.class)))
                .isTrue();
    }

    @Test
    public void voidIsNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailReturn", ViolationCollector.class)))
                .isFalse();
    }

    @Test
    public void privateIsNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailPrivate", ViolationCollector.class)))
                .isFalse();
    }

    @Test
    public void additionalParametersAreNotAccepted() throws Exception {
        assertThat(selfValidatingValidator.isMethodCorrect(
                getMethod("validateFailAdditionalParameters", ViolationCollector.class, int.class)))
                .isFalse();
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
