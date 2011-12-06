package com.yammer.dropwizard.jersey.params.tests;

import com.yammer.dropwizard.jersey.params.BooleanParam;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BooleanParamTest {
    @Test
    public void trueReturnsTrue() throws Exception {
        final BooleanParam param = new BooleanParam("true");

        assertThat(param.get(),
                   is(true));
    }

    @Test
    public void uppercaseTrueReturnsTrue() throws Exception {
        final BooleanParam param = new BooleanParam("TRUE");

        assertThat(param.get(),
                   is(true));
    }

    @Test
    public void falseReturnsFalse() throws Exception {
        final BooleanParam param = new BooleanParam("false");
        
        assertThat(param.get(),
                   is(false));
    }

    @Test
    public void uppercaseFalseReturnsFalse() throws Exception {
        final BooleanParam param = new BooleanParam("FALSE");
        
        assertThat(param.get(),
                   is(false));
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void nullThrowsAnException() throws Exception {
        try {
            new BooleanParam(null);
            fail("expected a WebApplicationException, but none was thrown");
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();
            
            assertThat(response.getStatus(),
                       is(400));
            
            assertThat((String) response.getEntity(),
                       is("\"null\" must be \"true\" or \"false\"."));
        }
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void nonBooleanValuesThrowAnException() throws Exception {
        try {
            new BooleanParam("foo");
            fail("expected a WebApplicationException, but none was thrown");
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus(),
                       is(400));

            assertThat((String) response.getEntity(),
                       is("\"foo\" must be \"true\" or \"false\"."));
        }
    }
}
