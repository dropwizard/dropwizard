package com.yammer.dropwizard.util;

import java.io.File;
import java.net.URL;

// TODO: 10/12/11 <coda> -- write tests for JarLocation
// TODO: 10/12/11 <coda> -- write docs for JarLocation

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
        } catch (Exception e) {
            return "project.jar";
        }
    }
}
