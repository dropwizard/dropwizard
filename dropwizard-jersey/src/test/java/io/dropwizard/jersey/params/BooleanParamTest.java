package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BooleanParamTest {
    private void booleanParamNegativeTest(@Nullable String input) {
        assertThatThrownBy(() -> new BooleanParam(input))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "Parameter must be \"true\" or \"false\".")
                );
            });
    }

    @Test
    public void trueReturnsTrue() {
        final BooleanParam param = new BooleanParam("true");

        assertThat(param.get())
                .isTrue();
    }

    @Test
    public void uppercaseTrueReturnsTrue() {
        final BooleanParam param = new BooleanParam("TRUE");

        assertThat(param.get())
                .isTrue();
    }

    @Test
    public void falseReturnsFalse() {
        final BooleanParam param = new BooleanParam("false");

        assertThat(param.get())
                .isFalse();
    }

    @Test
    public void uppercaseFalseReturnsFalse() {
        final BooleanParam param = new BooleanParam("FALSE");

        assertThat(param.get())
                .isFalse();
    }

    @Test
    public void nullThrowsAnException() {
        booleanParamNegativeTest(null);
    }

    @Test
    public void nonBooleanValuesThrowAnException() {
        booleanParamNegativeTest("foo");
    }
}
