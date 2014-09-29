package io.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A Jersey provider for OAuth2 bearer tokens.
 *
 * @param <T> the principal type.
 */
public final class OAuthFactory<T> extends AuthFactory<String, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthFactory.class);
    private static final String PREFIX = "Bearer";
    private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";

    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm;

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

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public AuthFactory<String, T> clone(boolean required) {
        return new OAuthFactory<>(required, authenticator(), this.realm, this.generatedClass);
    }

    @Override
    public T provide() {
        try {
            final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null) {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (PREFIX.equalsIgnoreCase(method)) {
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
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, realm))
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("Credentials are required to access this resource.")
                    .build());
        }

        return null;
    }

    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}