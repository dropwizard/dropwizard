package com.yammer.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.logging.Log;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

class OAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
    private static final Log LOG = Log.forClass(OAuthInjectable.class);
    private static final String HEADER_NAME = "WWW-Authenticate";
    private static final String HEADER_VALUE = "Bearer realm=\"%s\"";
    private static final String PREFIX = "bearer";

    private final Authenticator<String, T> authenticator;
    private final String realm;

    OAuthInjectable(Authenticator<String, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public T getValue(HttpContext c) {
        final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
        try {
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (PREFIX.equalsIgnoreCase(method)) {
                        final String credentials = B64Code.decode(header.substring(space + 1),
                                                                  StringUtil.__ISO_8859_1);
                        final Optional<T> result = authenticator.authenticate(credentials);
                        if (result.isPresent()) {
                            return result.get();
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOG.debug(e, "Error decoding credentials");
        } catch (AuthenticationException e) {
            LOG.warn(e, "Error authentication credentials");
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                                                  .header(HEADER_NAME,
                                                          String.format(HEADER_VALUE, realm))
                                                  .entity("Credentials are required to access this resource.")
                                                  .type(MediaType.TEXT_PLAIN_TYPE)
                                                  .build());
    }
}
