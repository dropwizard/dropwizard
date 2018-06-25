package com.example.helloworld.auth;

import com.example.helloworld.core.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.util.Sets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
    /**
     * Valid users with mapping user -> roles
     */
    private static final Map<String, Set<String>> VALID_USERS;

    static {
        final Map<String, Set<String>> validUsers = new HashMap<>();
        validUsers.put("guest", Collections.emptySet());
        validUsers.put("good-guy", Collections.singleton("BASIC_GUY"));
        validUsers.put("chief-wizard", Sets.of("ADMIN", "BASIC_GUY"));
        VALID_USERS = Collections.unmodifiableMap(validUsers);
    }

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (VALID_USERS.containsKey(credentials.getUsername()) && "secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername(), VALID_USERS.get(credentials.getUsername())));
        }
        return Optional.empty();
    }
}
