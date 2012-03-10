package com.yammer.dropwizard.auth;

public class User {
    private final String token;

    public User(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if ((o == null) || (getClass() != o.getClass())) { return false; }
        final User user = (User) o;
        return token.equals(user.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
