package io.dropwizard.auth.basic;

import com.google.common.base.MoreObjects;

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
        return Objects.hash(username, password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BasicCredentials other = (BasicCredentials) obj;
        return Objects.equals(this.username, other.username) && Objects.equals(this.password, other.password);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("password", "**********")
                .toString();
    }
}
