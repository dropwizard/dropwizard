package io.dropwizard.auth.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentials;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class AuthUtil {

    public static Authenticator<BasicCredentials, Principal> getTestAuthenticatorBasicCredential(final String validUser) {
        return new Authenticator<BasicCredentials, Principal>() {
            @Override
            public Optional<Principal> authenticate(BasicCredentials credentials) throws AuthenticationException {
                if (validUser.equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.<Principal>of(new PrincipalImpl(validUser));
                }
                if ("bad-guy".equals(credentials.getUsername())) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };
    }

    public static Authenticator<String, Principal> getTestAuthenticator(final String presented, final String returned) {
        return new Authenticator<String, Principal>() {
            @Override
            public Optional<Principal> authenticate(String user) throws AuthenticationException {
                if (presented.equals(user)) {
                    return Optional.<Principal>of(new PrincipalImpl(returned));
                }
                if ("bad-guy".equals(user)) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };
    }

    public static Authenticator<String, Principal> getTestAuthenticator(final String presented) {
        return getTestAuthenticator(presented, presented);
    }

    public static Function<AuthFilter.Tuple, SecurityContext> getSecurityContextProviderFunction(
            final String validUser,
            final String validRole
    ) {
        return new Function<AuthFilter.Tuple, SecurityContext>() {
            @Override
            public SecurityContext apply(final AuthFilter.Tuple input) {
                return new SecurityContext() {

                    @Override
                    public Principal getUserPrincipal() {
                        return input.getPrincipal();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return getUserPrincipal() != null
                                && validUser.equals(getUserPrincipal().getName())
                                && validRole.equals(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return input.getContainerRequestContext().getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }
                };
            }
        };
    }
}
