package com.yammer.dropwizard.validation.tests;

import com.yammer.dropwizard.validation.ValidationMethod;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;

import javax.validation.Valid;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({"FieldMayBeFinal","MethodMayBeStatic","UnusedDeclaration"})
public class MethodValidatorTest {
    public static class SubExample {
        @ValidationMethod(message = "also needs something special")
        public boolean isOK() {
            return false;
        }
    }

    public static class Example {
        @Valid
        private SubExample subExample = new SubExample();

        @ValidationMethod(message = "must have a false thing")
        public boolean isFalse() {
            return false;
        }

        @ValidationMethod(message = "must have a true thing")
        public boolean isTrue() {
            return true;
        }
    }

    @Test
    public void complainsAboutMethodsWhichReturnFalse() throws Exception {
        assertThat(new Validator().validate(new Example()))
                .containsOnly("must have a false thing",
                              "subExample also needs something special");
    }
}
