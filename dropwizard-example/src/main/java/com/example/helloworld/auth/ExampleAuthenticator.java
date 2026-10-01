package com.example.helloworld.auth;

import com.example.helloworld.core.User;
import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.types.Argon2;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
    private static final Argon2Function TARGET_HASHING_CONFIGURATION
        = Argon2Function.getInstance(14, 30, 1, 32, Argon2.ID);

    /**
     * Valid users with mapping user -> username, password hash, roles
     */
    private static final Map<String, UserAccountRecord> VALID_USERS = Stream.of(
            new UserAccountRecord(
                "restricted",
                Password.hash("secret")
                    .with(TARGET_HASHING_CONFIGURATION),
                Set.of()),
            new UserAccountRecord(
                "good-guy",
                Password.hash("secret")
                    .with(TARGET_HASHING_CONFIGURATION),
                Set.of("BASIC_GUY")),
            new UserAccountRecord(
                "chief-wizard",
                Password.hash("secret")
                    .with(TARGET_HASHING_CONFIGURATION),
                Set.of("ADMIN", "BASIC_GUY")))
        .collect(Collectors.toMap(
            record -> record.getUsername(),
            record -> record));

    private final Hash nonexistingUserPasswordHash = Password.hash("placeholder-password")
        .with(TARGET_HASHING_CONFIGURATION);

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        // look up user record from hardcoded user list
        final Optional<UserAccountRecord> userRecord = Optional.ofNullable(VALID_USERS.get(credentials.getUsername()));

        // retrieve correct password hash for this user if it exists, else fall back to placeholder hash
        final Hash correctPassword = userRecord.map(UserAccountRecord::getHashedPassword)
            .orElse(nonexistingUserPasswordHash);

        // verify password hash
        final boolean matches = Password.check(credentials.getPassword(), correctPassword);

        // Check user's existence/status after hashing so all paths take about the same time.
        if (!matches || userRecord.isEmpty()) {
            return Optional.empty();
        }
        final UserAccountRecord presentUserRecord = userRecord.get();

        // discard full user object and replace with limited Principal (user and roles)
        return Optional.of(new User(presentUserRecord.getUsername(), presentUserRecord.getRoles()));
    }
}
