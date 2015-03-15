package io.dropwizard.auth;

import org.glassfish.jersey.server.model.AnnotatedMethod;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

public class AuthDynamicFeature implements DynamicFeature {
    private ContainerRequestFilter authFilter;
    public AuthDynamicFeature(ContainerRequestFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        if(am.isAnnotationPresent(RolesAllowed.class) || am.isAnnotationPresent(DenyAll.class)) {
            context.register(authFilter);
        }
    }
}
