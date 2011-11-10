package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;

import javax.ws.rs.core.HttpHeaders;

public class OauthTokenInjectable extends AbstractHttpContextInjectable<Optional<String>> {
    private final String prefix;

    public OauthTokenInjectable(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Optional<String> getValue(HttpContext c) {
        final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
        if ((header != null) && header.startsWith(prefix)) {
            return Optional.of(header.substring(prefix.length()));
        }
        return Optional.absent();
    }
}
