package com.yammer.dropwizard.validation.tests;

import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;

public class ValidatorTest {
    @SuppressWarnings("unused")
    public static class Example {
        @NotNull
        private String notNull = null;

        @Max(30)
        private int tooBig = 50;

        public void setNotNull(String notNull) {
            this.notNull = notNull;
        }

        public void setTooBig(int tooBig) {
            this.tooBig = tooBig;
        }
    }

    private final Validator validator = new Validator();

    @Test
    public void returnsASetOfErrorsForAnObject() throws Exception {
        if ("en".equals(Locale.getDefault().getLanguage())) {
            assertThat(validator.validate(new Example()))
                    .containsOnly("notNull may not be null (was null)",
                                  "tooBig must be less than or equal to 30 (was 50)");
        }
    }

    @Test
    public void returnsAnEmptySetForAValidObject() throws Exception {
        final Example example = new Example();
        example.setNotNull("woo");
        example.setTooBig(20);

        assertThat(validator.validate(example))
                .isEmpty();
    }
}
