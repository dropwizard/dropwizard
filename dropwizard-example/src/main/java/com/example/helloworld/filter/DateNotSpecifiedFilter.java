package com.example.helloworld.filter;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class DateNotSpecifiedFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String dateHeader = requestContext.getHeaderString(HttpHeaders.DATE);
        if (dateHeader == null) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Date Header was not specified"), Response.Status.BAD_REQUEST);
        }
    }
}
