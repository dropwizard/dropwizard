package io.dropwizard.auth.util;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentials;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public class AuthUtil {

    public static Authenticator<BasicCredentials, Principal> getBasicAuthenticator(final List<String> validUsers) {
        return credentials -> {
            if (validUsers.contains(credentials.getUsername()) && "secret".equals(credentials.getPassword())) {
                return Optional.<Principal>of(new PrincipalImpl(credentials.getUsername()));
            }
            if ("bad-guy".equals(credentials.getUsername())) {
                throw new AuthenticationException("CRAP");
            }
            return Optional.empty();
        };
    }

    public static Authenticator<String, Principal> getSingleUserOAuthAuthenticator(final String presented,
                                                                                   final String returned) {
        return user -> {
            if (presented.equals(user)) {
                return Optional.<Principal>of(new PrincipalImpl(returned));
            }
            if ("bad-guy".equals(user)) {
                throw new AuthenticationException("CRAP");
            }
            return Optional.empty();
        };
    }

    public static Authenticator<String, Principal> getMultiplyUsersOAuthAuthenticator(final List<String> validUsers) {
        return credentials -> {
            if (validUsers.contains(credentials)) {
                return Optional.<Principal>of(new PrincipalImpl(credentials));
            }
            if ("bad-guy".equals(credentials)) {
                throw new AuthenticationException("CRAP");
            }
            return Optional.empty();
        };
    }

    public static Authorizer<Principal> getTestAuthorizer(final String validUser,
                                                          final String validRole) {
        return (principal, role) -> principal != null
            && validUser.equals(principal.getName())
            && validRole.equals(role);
    }
}
