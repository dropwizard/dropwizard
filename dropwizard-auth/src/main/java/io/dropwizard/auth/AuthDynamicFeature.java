package io.dropwizard.auth;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

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
 */
public class AuthDynamicFeature implements Feature, DynamicFeature {

    private final ContainerRequestFilter authFilter;

    private final Class<? extends ContainerRequestFilter> authFilterClass;

    // We suppress the null away checks, as adding `@Nullable` to the auth
    // filter fields, causes Jersey to try and resolve the fields to a concrete
    // type (which subsequently fails).
    @SuppressWarnings("NullAway")
    public AuthDynamicFeature(ContainerRequestFilter authFilter) {
        this.authFilter = authFilter;
        this.authFilterClass = null;
    }

    @SuppressWarnings("NullAway")
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
            if (containsAuthAnnotation(parameterAnnotations[i])) {
                // Optional auth requires that a concrete AuthFilter be provided.
                if (parameterTypes[i].equals(Optional.class) && authFilter != null) {
                    registerAuthFilter(context, new WebApplicationExceptionCatchingFilter(authFilter));
                } else {
                    registerAuthFilter(context);
                }
                return;
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

    private boolean containsAuthAnnotation(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation instanceof Auth) {
                return true;
            }
        }
        return false;
    }

    private void registerAuthFilter(FeatureContext context) {
        registerAuthFilter(context, null);
    }

    private void registerAuthFilter(FeatureContext context, @Nullable ContainerRequestFilter decoratedAuthFilter) {
        if (decoratedAuthFilter != null) {
            context.register(decoratedAuthFilter);
        } else if (authFilter != null) {
            context.register(authFilter);
        } else if (authFilterClass != null) {
            context.register(authFilterClass);
        }
    }

    @Override
    public boolean configure(FeatureContext context) {
        try {
            if (authFilter != null) {
                final InjectionManager injectionManager = InjectionManagerProvider.getInjectionManager(context);
                if (injectionManager != null) {
                    AuthInjectionHelper.inject(injectionManager, authFilter);
                }
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }
}
