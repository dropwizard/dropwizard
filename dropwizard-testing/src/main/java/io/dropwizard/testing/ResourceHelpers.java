package io.dropwizard.testing;

import io.dropwizard.util.Resources;

import java.io.File;

/**
 * A set of helper methods for working with classpath resources.
 */
public class ResourceHelpers {
    private ResourceHelpers() { /* singleton */ }

    /**
     * Detects the absolute path of a class path resource.
     *
     * @param resourceClassPathLocation the filename of the class path resource
     * @return the absolute path to the denoted resource
     */
    public static String resourceFilePath(final String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }
}
