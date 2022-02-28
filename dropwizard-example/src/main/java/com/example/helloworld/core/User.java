package com.example.helloworld.core;

import java.security.Principal;
import java.util.Random;
import java.util.Set;

public class User implements Principal {
    private static final Random rng = new Random();

    private final String name;

    private final Set<String> roles;

    public User(String name) {
        this.name = name;
        this.roles = null;
    }

    public User(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return rng.nextInt(100);
    }

    public Set<String> getRoles() {
        return roles;
    }
}
