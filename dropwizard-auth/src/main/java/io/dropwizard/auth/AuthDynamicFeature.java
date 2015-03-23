package io.dropwizard.auth;

import org.glassfish.jersey.server.model.AnnotatedMethod;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthDynamicFeature implements DynamicFeature {
    final private AuthHandler authHandler;

    public AuthDynamicFeature(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        if (am.isAnnotationPresent(Auth.class)) {
            Auth auth = am.getAnnotation(Auth.class);
            context.register(new AuthFilter(authHandler, auth.required()));
        }
    }
}
