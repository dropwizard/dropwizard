package io.dropwizard.jersey.params;

import jakarta.ws.rs.WebApplicationException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AbstractParamTest {

    /**
     * A parameter that relies on {@link AbstractParam}'s default {@code errorMessage(Exception)},
     * which embeds the parsing exception text (and therefore the raw input) in the message.
     */
    private static class EchoingParam extends AbstractParam<String> {
        EchoingParam(@Nullable String input) {
            super(input);
        }

        EchoingParam(@Nullable String input, String parameterName) {
            super(input, parameterName);
        }

        @Override
        protected String parse(@Nullable String input) {
            throw new IllegalArgumentException("For input string: \"" + input + "\"");
        }
    }

    @Test
    void inputWithFormatSpecifiersIsNotInterpretedAsAFormatString() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new EchoingParam("%s%s"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage())
                .isEqualTo("Parameter is invalid: For input string: \"ParameterParameter\""));
    }

    @Test
    void inputMixingConversionSpecifiersDoesNotThrowAnUnexpectedException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new EchoingParam("%s%d", "customName"))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getMessage())
                .isEqualTo("customName is invalid: For input string: \"customName%d\""));
    }
}
