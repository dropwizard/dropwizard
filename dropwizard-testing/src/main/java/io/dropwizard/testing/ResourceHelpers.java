package io.dropwizard.testing;

import static io.dropwizard.util.Resources.getResource;

import java.io.File;

/**
 * A set of helper methods for working with classpath resources.
 */
public class ResourceHelpers {
    private ResourceHelpers() {
        /* singleton */
    }

    /**
     * Detects the absolute path of a class path resource.
     *
     * @param resourceClassPathLocation the filename of the class path resource
     * @return the absolute path to the denoted resource
     */
    public static String resourceFilePath(final String resourceClassPathLocation) {
        try {
            return new File(getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
