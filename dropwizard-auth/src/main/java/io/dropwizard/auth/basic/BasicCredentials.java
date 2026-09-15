package io.dropwizard.auth.basic;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A set of user-provided Basic Authentication credentials, consisting of a username and a
 * password.
 */
public class BasicCredentials {
    private final String username;
    private final String password;

    /**
     * Creates a new BasicCredentials with the given username and password.
     *
     * @param username the username
     * @param password the password
     */
    public BasicCredentials(String username, String password) {
        this.username = requireNonNull(username);
        this.password = requireNonNull(password);
    }

    /**
     * Returns the credentials' username.
     *
     * @return the credentials' username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the credentials' password.
     *
     * @return the credentials' password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        // Intentionally exclude the password from the hash.
        //
        // BasicCredentials is used as the cache key of CachingAuthenticator, which stores
        // credentials in a hash-map. Including the password in hashCode() would mean that
        // two credentials for the same user with *different* passwords land in different
        // buckets, so the constant-time equals() comparison (which uses MessageDigest.isEqual)
        // is never reached. An attacker who can measure cache-hit vs. bucket-miss timing would
        // gain a coarser timing oracle on the password.
        //
        // Excluding the password ensures that same-username lookups always enter the same
        // bucket, forcing the constant-time equals() path for every authentication attempt.
        // The Java hashCode/equals contract still holds: equal objects share the same username
        // and therefore the same hashCode.
        return username.hashCode();
    }

    /**
     * Returns {@code true} if both the username and password match.
     *
     * <p>The password comparison uses {@link MessageDigest#isEqual} — a constant-time
     * byte comparison — to eliminate a timing side-channel. Because {@link
     * io.dropwizard.auth.CachingAuthenticator} uses {@code BasicCredentials} as a
     * cache key, {@code equals} is on the critical authentication path; a short-circuit
     * {@link String#equals} comparison would leak prefix information about the stored
     * password to a remote attacker who can measure response latency.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BasicCredentials other = (BasicCredentials) obj;
        return Objects.equals(this.username, other.username)
                && MessageDigest.isEqual(
                       this.password.getBytes(StandardCharsets.UTF_8),
                       other.password.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public String toString() {
        return "BasicCredentials{username=" + username + ", password=**********}";
    }
}
