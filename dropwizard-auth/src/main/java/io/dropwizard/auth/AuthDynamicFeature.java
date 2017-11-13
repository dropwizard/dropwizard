package io.dropwizard.auth;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * A {@link DynamicFeature} that registers the provided auth filter
 * to resource methods annotated with the {@link RolesAllowed}, {@link PermitAll}
 * and {@link DenyAll} annotations.
 * <p>In conjunction with {@link RolesAllowedDynamicFeature} it enables
 * authorization <i>AND</i> authentication of requests on the annotated methods.</p>
 * <p>If authorization is not a concern, then {@link RolesAllowedDynamicFeature}
 * could be omitted. But to enable authentication, the {@link PermitAll} annotation
 * should be placed on the corresponding resource methods.</p>
 * <p>Note that registration of the filter will follow the semantics of
 * {@link FeatureContext#register(Class)} and {@link FeatureContext#register(Object)}:
 * passing the filter as a {@link Class} to the {@link #AuthDynamicFeature(Class)}
 * constructor will result in dependency injection, while objects passed to
 * the {@link #AuthDynamicFeature(ContainerRequestFilter)} will be used directly.</p>
 */
public class AuthDynamicFeature implements DynamicFeature {

    @Nullable
    private final ContainerRequestFilter authFilter;

    @Nullable
    private final Class<? extends ContainerRequestFilter> authFilterClass;

    public AuthDynamicFeature(ContainerRequestFilter authFilter) {
        this.authFilter = authFilter;
        this.authFilterClass = null;
    }

    public AuthDynamicFeature(Class<? extends ContainerRequestFilter> authFilterClass) {
        this.authFilter = null;
        this.authFilterClass = authFilterClass;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        final Class<?>[] parameterTypes = am.getParameterTypes();

        // First, check for any @Auth annotations on the method.
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (final Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Auth) {
                    // Optional auth requires that a concrete AuthFilter be provided.
                    if (parameterTypes[i].equals(Optional.class) && authFilter != null) {
                        context.register(new WebApplicationExceptionCatchingFilter(authFilter));
                        return;
                    } else {
                        registerAuthFilter(context);
                        return;
                    }
                }
            }
        }

        // Second, check for any authorization annotations on the class or method.
        // Note that @DenyAll shouldn't be attached to classes.
        final boolean annotationOnClass = (resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class) != null) ||
            (resourceInfo.getResourceClass().getAnnotation(PermitAll.class) != null);
        final boolean annotationOnMethod = am.isAnnotationPresent(RolesAllowed.class) || am.isAnnotationPresent(DenyAll.class) ||
            am.isAnnotationPresent(PermitAll.class);

        if (annotationOnClass || annotationOnMethod) {
            registerAuthFilter(context);
        }
    }

    private void registerAuthFilter(FeatureContext context) {
        if (authFilter != null) {
            context.register(authFilter);
        } else if (authFilterClass != null) {
            context.register(authFilterClass);
        }
    }
}
