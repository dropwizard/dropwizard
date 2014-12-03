package io.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import io.dropwizard.auth.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 * A Jersey provider for OAuth2 bearer tokens.
 *
 * @param <T> the principal type.
 */
public final class OAuthFactory<T> extends AuthFactory<String, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthFactory.class);

    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm;
    private String prefix = "Bearer";
    private UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

    @Context
    private HttpServletRequest request;

    public OAuthFactory(final Authenticator<String, T> authenticator,
                        final String realm,
                        final Class<T> generatedClass) {
        super(authenticator);
        this.required = false;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    private OAuthFactory(final boolean required,
                         final Authenticator<String, T> authenticator,
                         final String realm,
                         final Class<T> generatedClass) {
        super(authenticator);
        this.required = required;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    public OAuthFactory<T> prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public OAuthFactory<T> responseBuilder(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        return this;
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public AuthFactory<String, T> clone(boolean required) {
        return new OAuthFactory<>(required, authenticator(), this.realm, this.generatedClass).prefix(prefix).responseBuilder(unauthorizedHandler);
    }

    @Override
    public T provide() {
        try {
            final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (prefix.equalsIgnoreCase(method)) {
                        final String credentials = header.substring(space + 1);
                        final Optional<T> result = authenticator().authenticate(credentials);
                        if (result.isPresent()) {
                            return result.get();
                        }
                    }
                }
            }
        } catch (AuthenticationException e) {
            LOGGER.warn("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        }

        if (required) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        return null;
    }

    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}