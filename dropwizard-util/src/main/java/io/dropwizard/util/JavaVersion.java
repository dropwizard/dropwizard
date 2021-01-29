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
        final int numericVersion;
        try {
            numericVersion = Integer.parseInt(getJavaSpecVersion());
        } catch (NumberFormatException ignore) {
            return false;
        }
        return numericVersion >= 11;
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
