package io.dropwizard.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import javax.validation.Valid;
import javax.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf("isDefaultLocaleEnglish")
class PortRangeValidatorTest {
    @SuppressWarnings("PublicField")
    public static class Example {
        @PortRange
        public int port = 8080;

        @PortRange(min = 10000, max = 15000)
        public int otherPort = 10001;

        @PortRange
        public Integer nullablePort = 1;

        @Valid
        List<@PortRange Integer> ports = Collections.emptyList();
    }


    private final Validator validator = BaseValidator.newValidator();
    private final Example example = new Example();

    @Test
    void acceptsNonPrivilegedPorts() {
        example.port = 2048;

        assertThat(validator.validate(example))
                .isEmpty();
    }

    @Test
    void acceptsDynamicPorts() {
        example.port = 0;

        assertThat(validator.validate(example))
                .isEmpty();
    }

    @Test
    void rejectsNegativePorts() {
        example.port = -1;

        assertThat(ConstraintViolations.format(validator.validate(example)))
                .containsOnly("port must be between 1 and 65535");
    }

    @Test
    void allowsForCustomMinimumPorts() {
        example.otherPort = 8080;

        assertThat(ConstraintViolations.format(validator.validate(example)))
                .containsOnly("otherPort must be between 10000 and 15000");
    }

    @Test
    void allowsForCustomMaximumPorts() {
        example.otherPort = 16000;

        assertThat(ConstraintViolations.format(validator.validate(example)))
                .containsOnly("otherPort must be between 10000 and 15000");
    }

    @Test
    void rejectsInvalidPortsInList() {
        example.ports = Collections.singletonList(-1);
        assertThat(ConstraintViolations.format(validator.validate(example)))
            .containsOnly("ports[0].<list element> must be between 1 and 65535");
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNull() {
        example.nullablePort = null;
        assertThat(ConstraintViolations.format(validator.validate(example)))
            .isEmpty();
    }

    private static boolean isDefaultLocaleEnglish() {
        return "en".equals(Locale.getDefault().getLanguage());
    }
}
