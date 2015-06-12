package io.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
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
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public class OAuthCredentialAuthFilter<P extends Principal> extends AuthFilter<String, P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthCredentialAuthFilter.class);
    private OAuthCredentialAuthFilter() {
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String header = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            try {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (prefix.equalsIgnoreCase(method)) {
                        final String credentials = header.substring(space + 1);
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
                    }
                }
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    public static class Builder<APrincipal extends Principal, AAuthenticator extends Authenticator<String, APrincipal>>
            extends AuthFilterBuilder<String, APrincipal, OAuthCredentialAuthFilter<APrincipal>, AAuthenticator> {
        @Override
        public OAuthCredentialAuthFilter<APrincipal> buildAuthFilter() {
            if (realm == null || authenticator == null || prefix == null || authorizer == null) {
                throw new RuntimeException("Required auth filter parameters not set");
            }

            OAuthCredentialAuthFilter<APrincipal> oauthCredentialAuthFilter = new OAuthCredentialAuthFilter<>();
            oauthCredentialAuthFilter.setRealm(realm);
            oauthCredentialAuthFilter.setAuthenticator(authenticator);
            oauthCredentialAuthFilter.setPrefix(prefix);
            oauthCredentialAuthFilter.setAuthorizer(authorizer);
            return oauthCredentialAuthFilter;
        }
    }
}
