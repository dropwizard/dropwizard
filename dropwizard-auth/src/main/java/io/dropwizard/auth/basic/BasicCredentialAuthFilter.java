package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Priority;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public class BasicCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCredentialAuthFilter.class);

    private BasicCredentialAuthFilter() {
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String header = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
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
                            try {
                                final Optional<P> principal = authenticator.authenticate(credentials);
                                if (principal.isPresent()) {
                                    requestContext.setSecurityContext(new SecurityContext() {
                                        @Override
                                        public Principal getUserPrincipal() {
                                            return principal.get();
                                        }

                                        @Override
                                        public boolean isUserInRole(String role) {
                                            return authorizer.authorize(principal.get(), role);
                                        }

                                        @Override
                                        public boolean isSecure() {
                                            return requestContext.getSecurityContext().isSecure();
                                        }

                                        @Override
                                        public String getAuthenticationScheme() {
                                            return SecurityContext.BASIC_AUTH;
                                        }
                                    });
                                    return;
                                }
                            } catch (AuthenticationException e) {
                                LOGGER.warn("Error authenticating credentials", e);
                                throw new InternalServerErrorException();
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error decoding credentials", e);
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }


    /**
     * Builder for {@link BasicCredentialAuthFilter}.
     * <p>An {@link Authenticator} and an {@link Authorizer} must be provided during the building process.</p>
     *
     * @param <P> the principal
     */
    public static class Builder<P extends Principal> extends
            AuthFilterBuilder<BasicCredentials, P, BasicCredentialAuthFilter<P>> {

        @Override
        protected BasicCredentialAuthFilter<P> newInstance() {
            return new BasicCredentialAuthFilter<>();
        }
    }
}
