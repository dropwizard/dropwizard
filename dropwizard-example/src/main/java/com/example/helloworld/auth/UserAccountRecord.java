package com.example.helloworld.auth;

import com.password4j.Hash;

import java.util.Objects;
import java.util.Set;

public class UserAccountRecord {
    private final String username;
    private final Hash hashedPassword;
    private final Set<String> roles;

    public UserAccountRecord(String username,
                             Hash hashedPassword,
                             Set<String> roles) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public Hash getHashedPassword() {
        return hashedPassword;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserAccountRecord that = (UserAccountRecord) o;
        return Objects.equals(username, that.username)
            && Objects.equals(hashedPassword, that.hashedPassword)
            && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, hashedPassword, roles);
    }

    @Override
    public String toString() {
        return "UserAccountRecord{"
            + "username='" + username + '\''
            + ", hashedPassword=<redacted>"  // deliberately don't write out the hash
            + ", roles=" + roles
            + '}';
    }
}
