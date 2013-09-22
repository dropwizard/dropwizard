package io.dropwizard.jersey.params;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class BooleanParamTest {
    @Test
    public void trueReturnsTrue() throws Exception {
        final BooleanParam param = new BooleanParam("true");

        assertThat(param.get())
                .isTrue();
    }

    @Test
    public void uppercaseTrueReturnsTrue() throws Exception {
        final BooleanParam param = new BooleanParam("TRUE");

        assertThat(param.get())
                .isTrue();
    }

    @Test
    public void falseReturnsFalse() throws Exception {
        final BooleanParam param = new BooleanParam("false");

        assertThat(param.get())
                .isFalse();
    }

    @Test
    public void uppercaseFalseReturnsFalse() throws Exception {
        final BooleanParam param = new BooleanParam("FALSE");

        assertThat(param.get())
                .isFalse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void nullThrowsAnException() throws Exception {
        try {
            new BooleanParam(null);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            assertThat((String) response.getEntity())
                    .isEqualTo("\"null\" must be \"true\" or \"false\".");
        }
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void nonBooleanValuesThrowAnException() throws Exception {
        try {
            new BooleanParam("foo");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            assertThat(response.getEntity())
                    .isEqualTo("\"foo\" must be \"true\" or \"false\".");
        }
    }
}
