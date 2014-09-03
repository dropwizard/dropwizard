package com.example.helloworld.filter;

import com.google.common.collect.Lists;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.List;

public class DateNotSpecifiedFilterFactory implements ResourceFilterFactory {

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        boolean methodNeedsDateHeader = am.isAnnotationPresent(DateRequired.class);

        return methodNeedsDateHeader ? Lists.<ResourceFilter>newArrayList(new DateNotSpecifiedFilter()) :
                Lists.<ResourceFilter>newArrayList();
    }

    private static class DateNotSpecifiedFilter implements ResourceFilter {
        @Override
        public ContainerRequestFilter getRequestFilter() {
            return new ContainerRequestFilter() {
                @Override
                public ContainerRequest filter(ContainerRequest request) {
                    String dateHeader = request.getHeaderValue(HttpHeaders.DATE);

                    if (dateHeader == null) {
                        Exception cause = new IllegalArgumentException("Date Header was not specified");
                        throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
                    } else {
                        return request;
                    }
                }
            };
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return new ContainerResponseFilter() {
                @Override
                public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
                    return response;
                }
            };
        }
    }
}
