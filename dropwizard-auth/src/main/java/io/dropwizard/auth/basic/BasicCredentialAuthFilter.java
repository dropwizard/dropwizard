package io.dropwizard.auth.basic;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;

@Priority(Priorities.AUTHENTICATION)
public class BasicCredentialAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {

    private BasicCredentialAuthFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final BasicCredentials credentials =
                getCredentials(requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
            throw unauthorizedHandler.buildException(prefix, realm);
        }
    }

    /**
     * Parses a Base64-encoded value of the `Authorization` header
     * in the form of `Basic dXNlcm5hbWU6cGFzc3dvcmQ=`.
     *
     * @param header the value of the `Authorization` header
     * @return a username and a password as {@link BasicCredentials}
     */
    @Nullable
    private BasicCredentials getCredentials(String header) {
        if (header == null) {
            return null;
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = header.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            return null;
        }

        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(space + 1)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("Error decoding credentials", e);
            return null;
        }

        // Decoded credentials is 'username:password'
        final int i = decoded.indexOf(':');
        if (i <= 0) {
            return null;
        }

        final String username = decoded.substring(0, i);
        final String password = decoded.substring(i + 1);
        return new BasicCredentials(username, password);
    }

    /**
     * Builder for {@link BasicCredentialAuthFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
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
