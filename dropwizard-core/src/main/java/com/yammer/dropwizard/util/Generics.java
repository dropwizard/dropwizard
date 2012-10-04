package com.yammer.dropwizard.util;

import com.yammer.dropwizard.config.Configuration;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Generics {
    private Generics() { /* singleton */ }

    public static Class<?> getTypeParameter(@Nonnull Class<?> klass) {
        Type t = klass;
        while (t instanceof Class<?>) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        /* This is not guaranteed to work for all cases with convoluted piping
         * of type parameters: but it can at least resolve straight-forward
         * extension with single type parameter (as per [Issue-89]).
         * And when it fails to do that, will indicate with specific exception.
         */
        if (t instanceof ParameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (Type param : ((ParameterizedType) t).getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    final Class<?> cls = (Class<?>) param;
                    if (Configuration.class.isAssignableFrom(cls)) {
                        return cls;
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot figure out Configuration type parameterization for " +
                                                klass.getName());
    }
}
