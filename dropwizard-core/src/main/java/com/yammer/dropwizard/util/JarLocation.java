package com.yammer.dropwizard.util;

import java.io.File;
import java.net.URL;

/**
 * A class which encapsulates the location on the local filesystem of the JAR in which the current
 * code is executing.
 */
public class JarLocation {
    @Override
    public String toString() {
        final URL location = JarLocation.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            final String jar = new File(location.getFile()).getName();
            if (jar.endsWith(".jar")) {
                return jar;
            }
            return "project.jar";
        } catch (Exception ignored) {
            return "project.jar";
        }
    }
}
