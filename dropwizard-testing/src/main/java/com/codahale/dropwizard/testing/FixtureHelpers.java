package com.codahale.dropwizard.testing;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A set of helper method for fixture files.
 */
public class FixtureHelpers {
    private FixtureHelpers() { /* singleton */ }

    /**
     * Reads the given fixture file from {@code src/test/resources} and returns its contents as a
     * UTF-8 string.
     *
     * @param filename    the filename of the fixture file
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IOException if {@code filename} doesn't exist or can't be opened
     */
    public static String fixture(String filename) throws IOException {
        return fixture(filename, Charsets.UTF_8);
    }

    /**
     * Reads the given fixture file from {@code src/test/resources} and returns its contents as a
     * string.
     *
     * @param filename    the filename of the fixture file
     * @param charset     the character set of {@code filename}
     * @return the contents of {@code src/test/resources/{filename}}
     * @throws IOException if {@code filename} doesn't exist or can't be opened
     */
    private static String fixture(String filename, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(filename), charset).trim();
    }
}
