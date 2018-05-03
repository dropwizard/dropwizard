package io.dropwizard.validation.selfvalidating;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;

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
        for(ResolvedMethod m : annotatedType.getMemberMethods()) {
            if(hasSignature(m, name, params)) {
                return m;
            }
        }
        throw new IllegalStateException("Could not resolve method "+name+Arrays.toString(params)+" in "+InvalidExample.class);
    }

    private boolean hasSignature(ResolvedMethod m, String name, Class<?>[] params) {
        if(!m.getName().equals(name) || m.getArgumentCount() != params.length) {
            return false;
        }
        for(int i=0 ; i < params.length ; i++) {
            if(!m.getArgumentType(i).getErasedType().equals(params[i]))
                return false;
        }
        return true;
    }

}
