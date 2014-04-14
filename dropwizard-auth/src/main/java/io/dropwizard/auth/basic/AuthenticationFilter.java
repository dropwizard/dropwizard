package io.dropwizard.auth.basic;


import com.google.common.base.Optional;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

import static io.dropwizard.auth.basic.BasicAuthHelper.createUnauthorizedResponse;
import static io.dropwizard.auth.basic.BasicAuthHelper.getBasicCredentialsFromHeader;

;

public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final boolean requireAuthorizationHeader;
    private final String realm;
    private final Authenticator<BasicCredentials, UserIdentity> authenticator;

    public AuthenticationFilter(final Authenticator<BasicCredentials, UserIdentity> authenticator, final boolean requireAuthorizationHeader, final String realm) {
        this.authenticator = authenticator;
        this.requireAuthorizationHeader = requireAuthorizationHeader;
        this.realm = realm;
    }

    @Override
    public ContainerRequest filter(final ContainerRequest containerRequest) {
        final Optional<BasicCredentials> basicCredentialsOptional = getBasicCredentialsFromHeader(containerRequest);
        final Optional<UserIdentity> userIdentityOptional = authenticate(basicCredentialsOptional);

        if (requireAuthorizationHeader) {
            // Require credentials, but no user credentials are presented. Throw exception.
            if (!basicCredentialsOptional.isPresent()) {
                throw new WebApplicationException(createUnauthorizedResponse(realm));
            }

            // Require credentials, but no user credentials are presented. Throw exception.
            if (!userIdentityOptional.isPresent()) {
                throw new WebApplicationException(Status.FORBIDDEN);
            }
        }

        if (userIdentityOptional.isPresent()) {
            final UserIdentity userIdentity = userIdentityOptional.get();
            SecurityContext securityContext = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return userIdentity.getUserPrincipal();
                }

                @Override
                public boolean isUserInRole(String role) {
                    return userIdentity.isUserInRole(role, null);
                }

                @Override
                public boolean isSecure() {
                    return containerRequest.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return SecurityContext.BASIC_AUTH;
                }
            };
            containerRequest.setSecurityContext(securityContext);
        }

        return containerRequest;
    }

    public Optional<UserIdentity> authenticate(Optional<BasicCredentials> basicCredentials) {
        if (!basicCredentials.isPresent()) {
            return Optional.absent();
        }

        try {
            return authenticator.authenticate(basicCredentials.get());
        } catch (AuthenticationException e) {
            logger.warn("Error authenticating credentials", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
