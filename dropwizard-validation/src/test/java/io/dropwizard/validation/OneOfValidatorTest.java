package io.dropwizard.validation;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.dropwizard.validation.ConstraintViolations.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OneOfValidatorTest {
    @SuppressWarnings("UnusedDeclaration")
    public static class Example {
        @OneOf({"one", "two", "three"})
        private String basic = "one";

        @OneOf(value = {"one", "two", "three"}, ignoreCase = true)
        private String caseInsensitive = "one";

        @OneOf(value = {"one", "two", "three"}, ignoreWhitespace = true)
        private String whitespaceInsensitive = "one";

        @Valid
        private List<@OneOf({"one", "two", "three"}) String> basicList = Collections.singletonList("one");
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    void allowsExactElements() throws Exception {
        assertThat(format(validator.validate(new Example())))
                .isEmpty();
    }

    @Test
    void doesNotAllowOtherElements() throws Exception {
        assumeTrue("en".equals(Locale.getDefault().getLanguage()),
                "This test executes when the defined language is English ('en'). If not, it is skipped.");

        final Example example = new Example();
        example.basic = "four";

        assertThat(format(validator.validate(example)))
                .containsOnly("basic must be one of [one, two, three]");
    }

    @Test
    void doesNotAllowBadElementsInList() {
        final Example example = new Example();
        example.basicList = Collections.singletonList("four");

        assertThat(format(validator.validate(example)))
            .containsOnly("basicList[0].<list element> must be one of [one, two, three]");
    }

    @Test
    void optionallyIgnoresCase() throws Exception {
        final Example example = new Example();
        example.caseInsensitive = "ONE";

        assertThat(format(validator.validate(example)))
                .isEmpty();
    }

    @Test
    void optionallyIgnoresWhitespace() throws Exception {
        final Example example = new Example();
        example.whitespaceInsensitive = "   one  ";

        assertThat(format(validator.validate(example)))
                .isEmpty();
    }
}
