package com.codahale.dropwizard.server.errors;

import org.eclipse.jetty.io.EofException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class EarlyEOFExceptionMapperTest {

    @Test
    public void testToReponse() {
        EarlyEOFExceptionMapper mapper = new EarlyEOFExceptionMapper();
        Response reponse = mapper.toResponse(new EofException());

        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), reponse.getStatus());
    }
}