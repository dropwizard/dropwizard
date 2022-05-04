package com.example.helloworld.auth;

import com.example.helloworld.core.User;
import io.dropwizard.auth.Authorizer;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.ws.rs.container.ContainerRequestContext;

public class ExampleAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role, @Nullable ContainerRequestContext ctx) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }
}
