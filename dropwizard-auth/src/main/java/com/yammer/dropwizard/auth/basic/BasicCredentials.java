package com.yammer.dropwizard.auth.basic;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;

import java.security.MessageDigest;

import static com.google.common.base.Preconditions.checkNotNull;

public class BasicCredentials {
    private final String username;
    private final String password;

    public BasicCredentials(String username, String password) {
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if ((obj == null) || (getClass() != obj.getClass())) { return false; }
        final BasicCredentials that = (BasicCredentials) obj;
        // do a constant-time comparison here to prevent timing attacks
        final byte[] thisBytes = password.getBytes(Charsets.UTF_8);
        final byte[] thatBytes = that.password.getBytes(Charsets.UTF_8);
        return username.equals(that.username) && MessageDigest.isEqual(thisBytes, thatBytes);
    }

    @Override
    public int hashCode() {
        return (31 * username.hashCode()) + password.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("username", username)
                      .add("password", "**********")
                      .toString();
    }
}
