package io.dropwizard.validation;

import org.junit.Test;

import javax.validation.Validator;
import java.util.Locale;

import static io.dropwizard.validation.ConstraintViolations.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class OneOfValidatorTest {
    @SuppressWarnings("UnusedDeclaration")
    public static class Example {
        @OneOf({"one", "two", "three"})
        private String basic = "one";

        @OneOf(value = {"one", "two", "three"}, ignoreCase = true)
        private String caseInsensitive = "one";

        @OneOf(value = {"one", "two", "three"}, ignoreWhitespace = true)
        private String whitespaceInsensitive = "one";
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void allowsExactElements() throws Exception {
        assertThat(format(validator.validate(new Example())))
                .isEmpty();
    }

    @Test
    public void doesNotAllowOtherElements() throws Exception {
        assumeTrue("en".equals(Locale.getDefault().getLanguage()));

        final Example example = new Example();
        example.basic = "four";

        assertThat(format(validator.validate(example)))
                .containsOnly("basic must be one of [one, two, three]");
    }

    @Test
    public void optionallyIgnoresCase() throws Exception {
        final Example example = new Example();
        example.caseInsensitive = "ONE";

        assertThat(format(validator.validate(example)))
                .isEmpty();
    }

    @Test
    public void optionallyIgnoresWhitespace() throws Exception {
        final Example example = new Example();
        example.whitespaceInsensitive = "   one  ";

        assertThat(format(validator.validate(example)))
                .isEmpty();
    }
}
