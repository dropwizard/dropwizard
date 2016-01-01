package io.dropwizard.auth;

import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import java.lang.annotation.Annotation;
import java.security.Principal;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * A {@link DynamicFeature} that registers the provided auth filters
 * to resource methods annotated with the {@link Auth} according to
 * the type of the annotated method parameter.
 * <p>Note that this feature only pertains to <i>authentication</i>.
 * To enable authorization, use {@link AuthDynamicFeature}.</p>
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

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (final Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof Auth && authFilterMap.containsKey(parameterTypes[i])) {
                  context.register(authFilterMap.get(parameterTypes[i]));
                  return;
                }
            }
        }
    }
}
