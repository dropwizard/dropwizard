package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import io.dropwizard.auth.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.nio.charset.StandardCharsets;

/**
 * A Jersey provider for Basic HTTP authentication.
 *
 * @param <T> the principal type.
 */
public final class BasicAuthFactory<T> extends AuthFactory<BasicCredentials, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFactory.class);

    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm;
    private String prefix = "Basic";
    private UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

    @Context
    private HttpServletRequest request;

    public BasicAuthFactory(final Authenticator<BasicCredentials, T> authenticator,
                            final String realm,
                            final Class<T> generatedClass) {
        super(authenticator);
        this.required = false;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    private BasicAuthFactory(final boolean required,
                             final Authenticator<BasicCredentials, T> authenticator,
                             final String realm,
                             final Class<T> generatedClass) {
        super(authenticator);
        this.required = required;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    public BasicAuthFactory<T> prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public BasicAuthFactory<T> responseBuilder(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        return this;
    }

    @Override
    public AuthFactory<BasicCredentials, T> clone(boolean required) {
        return new BasicAuthFactory<>(required, authenticator(), this.realm, this.generatedClass).prefix(prefix).responseBuilder(unauthorizedHandler);
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public T provide() {
        if (request != null) {
            final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            try {
                if (header != null) {
                    final int space = header.indexOf(' ');
                    if (space > 0) {
                        final String method = header.substring(0, space);
                        if (prefix.equalsIgnoreCase(method)) {
                            final String decoded = new String(
                                    BaseEncoding.base64().decode(header.substring(space + 1)),
                                    StandardCharsets.UTF_8);
                            final int i = decoded.indexOf(':');
                            if (i > 0) {
                                final String username = decoded.substring(0, i);
                                final String password = decoded.substring(i + 1);
                                final BasicCredentials credentials = new BasicCredentials(username, password);
                                final Optional<T> result = authenticator().authenticate(credentials);
                                if (result.isPresent()) {
                                    return result.get();
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
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
