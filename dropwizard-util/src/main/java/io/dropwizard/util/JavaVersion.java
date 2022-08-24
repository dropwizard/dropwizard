package io.dropwizard.util;

import javax.annotation.Nullable;

/**
 * Provides helper methods related to the version of the executing java platform.
 * @since 2.0
 */
public final class JavaVersion {
    private JavaVersion() {
    }

    /**
     * Checks, if the execution platform is running a JRE 8.
     *
     * @return if the current Java version is 1.8
     */
    public static boolean isJava8() {
        final String specVersion = getJavaSpecVersion();
        return specVersion != null && specVersion.startsWith("1.8");
    }

    @Nullable
    private static String getJavaSpecVersion() {
        try {
            return System.getProperty("java.specification.version");
        } catch (final SecurityException ex) {
            return null;
        }
    }
}
