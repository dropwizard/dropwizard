package com.yammer.dropwizard.jersey.params.tests;

import com.yammer.dropwizard.jersey.params.LongParam;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class LongParamTest {
    @Test
    public void aLongReturnsALong() throws Exception {
        final LongParam param = new LongParam("200");

        assertThat(param.get())
                .isEqualTo(200L);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonIntegerThrowsAnException() throws Exception {
        try {
            new LongParam("foo");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            assertThat((String) response.getEntity())
                    .isEqualTo("\"foo\" is not a number.");
        }
    }
}
