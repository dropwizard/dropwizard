package io.dropwizard.util;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static java.util.Objects.requireNonNull;

/**
 * Helper methods for class type parameters.
 * @see <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html">Super Type Tokens</a>
 */
public class Generics {
    private Generics() { /* singleton */ }

    /**
     * Finds the type parameter for the given class.
     *
     * @param klass a parameterized class
     * @return the class's type parameter
     */
    public static Class<?> getTypeParameter(Class<?> klass) {
        return getTypeParameter(klass, Object.class);
    }

    /**
     * Finds the type parameter for the given class which is assignable to the bound class.
     *
     * @param klass a parameterized class
     * @param bound the type bound
     * @param <T>   the type bound
     * @return the class's type parameter
     */
    public static <T> Class<T> getTypeParameter(Class<?> klass, Class<? super T> bound) {
        Type t = requireNonNull(klass);
        while (t instanceof Class<?> classObj) {
            t = classObj.getGenericSuperclass();
        }
        /* This is not guaranteed to work for all cases with convoluted piping
         * of type parameters: but it can at least resolve straight-forward
         * extension with single type parameter (as per [Issue-89]).
         * And when it fails to do that, will indicate with specific exception.
         */
        if (t instanceof ParameterizedType parameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (Type param : parameterizedType.getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    final Class<T> cls = determineClass(bound, param);
                    if (cls != null) {
                        return cls;
                    }
                } else if (param instanceof TypeVariable<?> typeVariable) {
                    for (Type paramBound : typeVariable.getBounds()) {
                        if (paramBound instanceof Class<?>) {
                            final Class<T> cls = determineClass(bound, paramBound);
                            if (cls != null) {
                                return cls;
                            }
                        }
                    }
                } else if (param instanceof ParameterizedType parameterizedTypeParam) {
                    final Type rawType = parameterizedTypeParam.getRawType();
                    if (rawType instanceof Class<?>) {
                        final Class<T> cls = determineClass(bound, rawType);
                        if (cls != null) {
                            return cls;
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot figure out type parameterization for " + klass.getName());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> Class<T> determineClass(Class<? super T> bound, Type candidate) {
        if (candidate instanceof Class<?> cls) {
            if (bound.isAssignableFrom(cls)) {
                return (Class<T>) cls;
            }
        }

        return null;
    }
}
