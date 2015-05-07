package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public class BasicCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {
    final private static Logger LOGGER = LoggerFactory.getLogger(BasicCredentialAuthFilter.class);
    private BasicCredentialAuthFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
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
                                final Optional<P> result = authenticator.authenticate(credentials);
                                if (result.isPresent()) {
                                    Principal principal = result.get();
                                    requestContext.setSecurityContext(
                                            getSecurityContextFunction().apply(new Tuple(requestContext, principal)));
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
        }
        catch (IllegalArgumentException e) {
            LOGGER.warn("Error decoding credentials", e);
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }


    public static class Builder<APrincipal extends Principal, AAuthenticator extends Authenticator<BasicCredentials, APrincipal>>
            extends AuthFilterBuilder<BasicCredentials, APrincipal, BasicCredentialAuthFilter<APrincipal>, AAuthenticator> {

        @Override
        public BasicCredentialAuthFilter<APrincipal> buildAuthFilter() {
            if(realm == null || authenticator == null || prefix == null || securityContextFunction == null) {
                throw new RuntimeException("Required auth filter parameters not set");
            }

            BasicCredentialAuthFilter<APrincipal> basicCredentialAuthFilter= new BasicCredentialAuthFilter<>();
            basicCredentialAuthFilter.setRealm(realm);
            basicCredentialAuthFilter.setAuthenticator(authenticator);
            basicCredentialAuthFilter.setPrefix(prefix);
            basicCredentialAuthFilter.setSecurityContextFunction(securityContextFunction);
            return basicCredentialAuthFilter;
        }
    }
}
