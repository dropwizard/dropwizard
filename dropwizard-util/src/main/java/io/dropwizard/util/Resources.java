package io.dropwizard.util;

import java.net.URL;

/**
 * @since 2.0
 */
public final class Resources {
    private Resources() {}

    /**
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is found using the
     * {@linkplain Thread#getContextClassLoader() context class loader}. In simple environments, the
     * context class loader will find resources from the class path. In environments where different
     * threads can have different class loaders, for example app servers, the context class loader
     * will typically have been set to an appropriate loader for the current thread.
     *
     * <p>In the unusual case where the context class loader is null, the class loader that loaded
     * this class ({@code Resources}) will be used instead.
     *
     * @throws IllegalArgumentException if the resource is not found
     */
    public static URL getResource(String resourceName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader loader = contextClassLoader == null ? Resources.class.getClassLoader() : contextClassLoader;
        final URL url = loader.getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException("resource " + resourceName + " not found.");
        }
        return url;
    }
}
