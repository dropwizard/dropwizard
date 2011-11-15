package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.BearerToken;

import javax.ws.rs.ext.Provider;

// TODO: 11/14/11 <coda> -- test OauthTokenProvider
// TODO: 11/14/11 <coda> -- document OauthTokenProvider

@Provider
public class OauthTokenProvider implements InjectableProvider<BearerToken, Parameter> {
    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       BearerToken a,
                                       Parameter c) {
        if (c.getParameterClass().isAssignableFrom(Optional.class)) {
            return new OauthTokenInjectable(a.value() + ' ');
        }
        return null;
    }
}
