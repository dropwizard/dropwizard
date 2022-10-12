package io.dropwizard.util;

import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * A class which encapsulates the location on the local filesystem of the JAR in which the current
 * code is executing.
 */
public class JarLocation {
    private final Class<?> klass;

    /**
     * Constructs a new {@link JarLocation} object which gets access to the code source with the provided parameter.
     *
     * @param klass the class to access the code source from
     */
    public JarLocation(Class<?> klass) {
        this.klass = klass;
    }

    /**
     * Returns the version of the current jar holding the provided {@code klass}.
     *
     * @return the version representation
     */
    public Optional<String> getVersion() {
        return Optional.ofNullable(klass.getPackage())
            .map(Package::getImplementationVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final URL location = klass.getProtectionDomain().getCodeSource().getLocation();
        try {
            final String jar = new File(location.toURI()).getName();
            if (jar.endsWith(".jar")) {
                return jar;
            }
            return "project.jar";
        } catch (Exception ignored) {
            return "project.jar";
        }
    }
}
