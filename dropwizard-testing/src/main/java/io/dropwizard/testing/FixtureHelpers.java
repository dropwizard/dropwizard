package io.dropwizard.testing;

import io.dropwizard.util.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A set of helper method for fixture files.
 */
public class FixtureHelpers {
    private FixtureHelpers() { /* singleton */ }

    /**
     * Reads the given fixture file from the classpath (e. g. {@code src/test/resources})
     * and returns its contents as a UTF-8 string.
     *
     * @param filename the filename of the fixture file
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IllegalArgumentException if an I/O error occurs.
     */
    public static String fixture(String filename) {
        return fixture(filename, StandardCharsets.UTF_8);
    }

    /**
     * Reads the given fixture file from the classpath (e. g. {@code src/test/resources})
     * and returns its contents as a string.
     *
     * @param filename the filename of the fixture file
     * @param charset  the character set of {@code filename}
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IllegalArgumentException if an I/O error occurs.
     */
    private static String fixture(String filename, Charset charset) {
        final URL resource = Resources.getResource(filename);
        try {
            return Resources.toString(resource, charset).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
