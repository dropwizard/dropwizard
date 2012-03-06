package com.yammer.dropwizard.jersey.caching;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;

public class CacheControlledRequestDispatcher implements RequestDispatcher {
    private final RequestDispatcher dispatcher;
    private final CacheControl cacheControl;

    public CacheControlledRequestDispatcher(RequestDispatcher dispatcher, CacheControl cacheControl) {
        this.dispatcher = dispatcher;
        this.cacheControl = cacheControl;
    }

    @Override
    public void dispatch(Object resource, HttpContext context) {
        dispatcher.dispatch(resource, context);
        context.getResponse().getHttpHeaders().add(HttpHeaders.CACHE_CONTROL, cacheControl);
    }
}
