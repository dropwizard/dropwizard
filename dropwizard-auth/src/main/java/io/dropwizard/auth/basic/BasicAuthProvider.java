package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A Jersey provider for Basic HTTP authentication.
 *
 * @param <T> the principal type.
 */
public class BasicAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthProvider.class);

    private static class BasicAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private static final String PREFIX = "Basic";
        private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";

        private final Authenticator<BasicCredentials, T> authenticator;
        private final String realm;
        private final boolean required;

        private BasicAuthInjectable(Authenticator<BasicCredentials, T> authenticator,
                                    String realm,
                                    boolean required) {
            this.authenticator = authenticator;
            this.realm = realm;
            this.required = required;
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
                            final String decoded = B64Code.decode(header.substring(space + 1),
                                                                  StringUtil.__ISO_8859_1);
                            final int i = decoded.indexOf(':');
                            if (i > 0) {
                                final String username = decoded.substring(0, i);
                                final String password = decoded.substring(i + 1);
                                final BasicCredentials credentials = new BasicCredentials(username,
                                                                                          password);
                                final Optional<T> result = authenticator.authenticate(credentials);
                                if (result.isPresent()) {
                                    return result.get();
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }

            if (required) {
                final String challenge = String.format(CHALLENGE_FORMAT, realm);
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                                                          .header(HttpHeaders.WWW_AUTHENTICATE,
                                                                  challenge)
                                                          .entity("Credentials are required to access this resource.")
                                                          .type(MediaType.TEXT_PLAIN_TYPE)
                                                          .build());
            }
            return null;
        }
    }

    private final Authenticator<BasicCredentials, T> authenticator;
    private final String realm;

    /**
     * Creates a new BasicAuthProvider with the given {@link Authenticator} and realm.
     *
     * @param authenticator the authenticator which will take the {@link BasicCredentials} and
     *                      convert them into instances of {@code T}
     * @param realm         the name of the authentication realm
     */
    public BasicAuthProvider(Authenticator<BasicCredentials, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Auth a, Parameter c) {
        return new BasicAuthInjectable<>(authenticator, realm, a.required());
    }
}
