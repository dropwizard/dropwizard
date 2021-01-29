package io.dropwizard.util;

import javax.annotation.Nullable;

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

    public static boolean isJava11OrHigher() {
        try {
            return Integer.parseInt(getJavaSpecVersion()) >= 11;
        } catch (NumberFormatException ignore) {
            return false;
        }
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
