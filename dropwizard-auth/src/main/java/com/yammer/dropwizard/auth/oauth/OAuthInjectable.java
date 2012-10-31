package com.yammer.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

class OAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthInjectable.class);
    private static final String HEADER_NAME = "WWW-Authenticate";
    private static final String HEADER_VALUE = "Bearer realm=\"%s\"";
    private static final String PREFIX = "bearer";

    private final Authenticator<String, T> authenticator;
    private final String realm;
    private final boolean required;

    OAuthInjectable(Authenticator<String, T> authenticator, String realm, boolean required) {
        this.authenticator = authenticator;
        this.realm = realm;
        this.required = required;
    }

    public Authenticator<String, T> getAuthenticator() {
        return authenticator;
    }

    public String getRealm() {
        return realm;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public T getValue(HttpContext c) {
        try {
            final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (PREFIX.equalsIgnoreCase(method)) {
                        final String credentials = header.substring(space + 1);
                        final Optional<T> result = authenticator.authenticate(credentials);
                        if (result.isPresent()) {
                            return result.get();
                        }
                    }
                }
            }
        } catch (AuthenticationException e) {
            LOGGER.warn("Error authenticating credentials", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        if (required) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                                                      .header(HEADER_NAME,
                                                              String.format(HEADER_VALUE, realm))
                                                      .entity("Credentials are required to access this resource.")
                                                      .type(MediaType.TEXT_PLAIN_TYPE)
                                                      .build());
        }
        return null;
    }
}
