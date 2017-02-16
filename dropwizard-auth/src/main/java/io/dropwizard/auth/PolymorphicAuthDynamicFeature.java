package io.dropwizard.auth;

import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * A {@link DynamicFeature} that registers the provided auth filters
 * to resource methods annotated with the {@link Auth} according to
 * the type of the annotated method parameter.
 */
public class PolymorphicAuthDynamicFeature<T extends Principal> implements DynamicFeature {

    private final ImmutableMap<Class<? extends T>,  ContainerRequestFilter> authFilterMap;

    public PolymorphicAuthDynamicFeature(
        ImmutableMap<Class<? extends T>,  ContainerRequestFilter> authFilterMap
    ) {
        this.authFilterMap = authFilterMap;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        final Class<?>[] parameterTypes = am.getParameterTypes();
        final Type[] parameterGenericTypes = am.getGenericParameterTypes();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (final Annotation annotation : parameterAnnotations[i]) {
                // If the parameter type is an Optional, extract its type
                // parameter. Otherwise, use the parameter type itself.
                final Type paramType = (parameterTypes[i].equals(Optional.class))
                    ? ((ParameterizedType) parameterGenericTypes[i]).getActualTypeArguments()[0]
                    : parameterTypes[i];

                if (annotation instanceof Auth && authFilterMap.containsKey(paramType)) {
                    if (parameterTypes[i].equals(Optional.class)) {
                        context.register(new WebApplicationExceptionCatchingFilter(authFilterMap.get(paramType)));
                        return;
                    } else {
                        context.register(authFilterMap.get(parameterTypes[i]));
                        return;
                    }
                }
            }
        }
    }
}
