package com.yammer.dropwizard.jersey.caching;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

public class CacheControlledResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new CacheControlledResourceMethodDispatchProvider(provider);
    }
}
