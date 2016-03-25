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

    public JarLocation(Class<?> klass) {
        this.klass = klass;
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(klass.getPackage())
            .map(Package::getImplementationVersion);
    }

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
