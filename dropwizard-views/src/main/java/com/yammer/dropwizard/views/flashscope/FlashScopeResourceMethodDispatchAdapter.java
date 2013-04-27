package com.yammer.dropwizard.views.flashscope;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class FlashScopeResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {

    private final FlashScopeConfig config;

    public FlashScopeResourceMethodDispatchAdapter(FlashScopeConfig config) {
        this.config = config;
    }

    public FlashScopeResourceMethodDispatchAdapter() {
        this(new FlashScopeConfig());
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new FlashScopeResourceMethodDispatchProvider(config, provider);
    }
}
