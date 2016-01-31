package io.dropwizard.auth;

import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;

/**
 * A {@link DynamicFeature} that registers the provided auth filter
 * to resource methods annotated with the {@link RolesAllowed}, {@link PermitAll}
 * and {@link DenyAll} annotations.
 * <p>In conjunction with {@link RolesAllowedDynamicFeature} it enables
 * authorization <i>AND</i> authentication of requests on the annotated methods.</p>
 * <p>If authorization is not a concern, then {@link RolesAllowedDynamicFeature}
 * could be omitted. But to enable authentication, the {@link PermitAll} annotation
 * should be placed on the corresponding resource methods.</p>
 */
public class AuthDynamicFeature implements DynamicFeature {

    private final ContainerRequestFilter authFilter;

    public AuthDynamicFeature(ContainerRequestFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        //@DenyAll shouldn't be attached to classes
        final boolean annotationOnClass = (resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class) != null) ||
            (resourceInfo.getResourceClass().getAnnotation(PermitAll.class) != null);
        final boolean annotationOnMethod = am.isAnnotationPresent(RolesAllowed.class) || am.isAnnotationPresent(DenyAll.class) ||
            am.isAnnotationPresent(PermitAll.class);

        if (annotationOnClass || annotationOnMethod) {
            context.register(authFilter);
        } else {
            for (Annotation[] annotations : parameterAnnotations) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Auth) {
                        context.register(authFilter);
                        return;
                    }
                }
            }
        }
    }
}
