package com.yammer.dropwizard.jersey.params.tests;

import com.yammer.dropwizard.jersey.params.IntParam;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IntParamTest {
    @Test
    public void anIntegerReturnsAnInteger() throws Exception {
        final IntParam param = new IntParam("200");

        assertThat(param.get(),
                   is(200));
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonIntegerThrowsAnException() throws Exception {
        try {
            new IntParam("foo");
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
