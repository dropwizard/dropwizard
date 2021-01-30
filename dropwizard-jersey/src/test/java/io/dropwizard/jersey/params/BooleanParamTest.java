package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BooleanParamTest {

    @Test
    public void trueReturnsTrue() {
        assertThat(new BooleanParam("true").get()).isTrue();
    }

    @Test
    public void uppercaseTrueReturnsTrue() {
        assertThat(new BooleanParam("TRUE").get()).isTrue();
    }

    @Test
    public void falseReturnsFalse() {
        assertThat(new BooleanParam("false").get()).isFalse();
    }

    @Test
    public void uppercaseFalseReturnsFalse() {
        assertThat(new BooleanParam("FALSE").get()).isFalse();
    }

    @Test
    public void nullThrowsAnException() {
        booleanParamNegativeTest(null);
    }

    @Test
    public void nonBooleanValuesThrowAnException() {
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
