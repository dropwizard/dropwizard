package com.yammer.dropwizard.jersey.params.tests;

import com.yammer.dropwizard.jersey.params.LongParam;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LongParamTest {
    @Test
    public void aLongReturnsALong() throws Exception {
        final LongParam param = new LongParam("200");

        assertThat(param.get(),
                   is(200L));
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonIntegerThrowsAnException() throws Exception {
        try {
            new LongParam("foo");
            fail("expected a WebApplicationException, but none was thrown");
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus(),
                       is(400));

            assertThat((String) response.getEntity(),
                       is("\"foo\" is not a number."));
        }
    }
}
