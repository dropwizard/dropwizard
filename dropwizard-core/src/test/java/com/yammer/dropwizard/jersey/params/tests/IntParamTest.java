package com.yammer.dropwizard.jersey.params.tests;

import com.yammer.dropwizard.jersey.params.IntParam;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class IntParamTest {
    @Test
    public void anIntegerReturnsAnInteger() throws Exception {
        final IntParam param = new IntParam("200");

        assertThat(param.get())
                .isEqualTo(200);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonIntegerThrowsAnException() throws Exception {
        try {
            new IntParam("foo");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            assertThat(response.getEntity())
                    .isEqualTo("\"foo\" is not a number.");
        }
    }
}
