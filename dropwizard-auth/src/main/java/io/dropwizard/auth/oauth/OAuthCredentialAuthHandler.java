package io.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.AuthHandler;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import java.security.Principal;

public class OAuthCredentialAuthHandler<P extends Principal> extends AuthHandler<String, P> {
    final private static Logger LOGGER = LoggerFactory.getLogger(OAuthCredentialAuthHandler.class);
    private OAuthCredentialAuthHandler() {}

    public void handle(ContainerRequestContext requestContext, boolean authRequired) {
        final String header = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            try {
                final int space = header.indexOf(' ');
                if (space > 0) {
                    final String method = header.substring(0, space);
                    if (prefix.equalsIgnoreCase(method)) {
                        final String credentials = header.substring(space + 1);
                        final Optional<P> result = authenticator.authenticate(credentials);
                        if (result.isPresent()) {
                            final Principal principal = result.get();
                            requestContext.setSecurityContext(
                                    getSecurityContextFunction().apply(new Tuple(requestContext, principal)));
                            return;
                        }
                    }
                }
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
        }

        if (authRequired) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }
    }

    public static class Builder<APrincipal extends Principal, AAuthenticator extends Authenticator<String, APrincipal>>
            extends AuthHandler.AuthHandlerBuilder<String, APrincipal, OAuthCredentialAuthHandler<APrincipal>, AAuthenticator> {
        @Override
        public OAuthCredentialAuthHandler<APrincipal> buildAuthHandler() {
            OAuthCredentialAuthHandler<APrincipal> oauthCredentialAuthHandler = new OAuthCredentialAuthHandler<>();
            oauthCredentialAuthHandler.setRealm(realm);
            oauthCredentialAuthHandler.setAuthenticator(authenticator);
            oauthCredentialAuthHandler.setPrefix(prefix);
            oauthCredentialAuthHandler.setSecurityContextFunction(securityContextFunction);
            oauthCredentialAuthHandler.setUnauthorizedHandler(unauthorizedHandler);
            return oauthCredentialAuthHandler;
        }
    }
}
