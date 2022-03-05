package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import jakarta.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BooleanParamTest {

    @Test
    void trueReturnsTrue() {
        assertThat(new BooleanParam("true").get()).isTrue();
    }

    @Test
    void uppercaseTrueReturnsTrue() {
        assertThat(new BooleanParam("TRUE").get()).isTrue();
    }

    @Test
    void falseReturnsFalse() {
        assertThat(new BooleanParam("false").get()).isFalse();
    }

    @Test
    void uppercaseFalseReturnsFalse() {
        assertThat(new BooleanParam("FALSE").get()).isFalse();
    }

    @Test
    void nullThrowsAnException() {
        booleanParamNegativeTest(null);
    }

    @Test
    void nonBooleanValuesThrowAnException() {
        booleanParamNegativeTest("foo");
    }

    private void booleanParamNegativeTest(@Nullable String input) {
        final ErrorMessage expected = new ErrorMessage(400, "Parameter must be \"true\" or \"false\".");
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new BooleanParam(input))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(expected));
    }
}
