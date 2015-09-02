package io.dropwizard.auth;

import javax.ws.rs.container.ContainerRequestFilter;
import java.security.Principal;

public abstract class AuthBaseResourceConfig extends AbstractAuthResourceConfig {

    public AuthBaseResourceConfig() {
        register(AuthResource.class);
    }

    protected abstract ContainerRequestFilter getAuthFilter();

    protected Class<? extends Principal> getPrincipalClass() {
        return Principal.class;
    }
}
