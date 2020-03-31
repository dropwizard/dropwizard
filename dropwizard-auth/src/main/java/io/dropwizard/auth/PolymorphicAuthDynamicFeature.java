package io.dropwizard.auth;

import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link DynamicFeature} that registers the provided auth filters
 * to resource methods annotated with the {@link Auth} according to
 * the type of the annotated method parameter.
 */
public class PolymorphicAuthDynamicFeature<T extends Principal> implements DynamicFeature {

    private final Map<Class<? extends T>,  ContainerRequestFilter> authFilterMap;

    public PolymorphicAuthDynamicFeature(Map<Class<? extends T>,  ContainerRequestFilter> authFilterMap) {
        this.authFilterMap = authFilterMap;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        final Annotation[][] parameterAnnotations = am.getParameterAnnotations();
        final Class<?>[] parameterTypes = am.getParameterTypes();
        final Type[] parameterGenericTypes = am.getGenericParameterTypes();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            final Class<?> type = parameterTypes[i];

            // If the parameter type is an Optional, extract its type
            // parameter. Otherwise, use the parameter type itself.
            final Type paramType = type == Optional.class
                ? ((ParameterizedType) parameterGenericTypes[i]).getActualTypeArguments()[0]
                : type;

            for (final Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Auth && authFilterMap.containsKey(paramType)) {
                    if (type == Optional.class) {
                        final ContainerRequestFilter filter = authFilterMap.get(paramType);
                        context.register(new WebApplicationExceptionCatchingFilter(filter));
                        return;
                    } else {
                        context.register(authFilterMap.get(type));
                        return;
                    }
                }
            }
        }
    }
}
