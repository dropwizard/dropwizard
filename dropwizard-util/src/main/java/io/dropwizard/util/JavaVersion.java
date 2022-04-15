package io.dropwizard.util;

import org.jetbrains.annotations.Nullable;

/**
 * @since 2.0
 */
public final class JavaVersion {
    private JavaVersion() {
    }

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
