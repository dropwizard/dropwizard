package io.dropwizard.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;

public final class Resources {
    private Resources() {
    }

    /**
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is found using the
     * {@linkplain Thread#getContextClassLoader() context class loader}. In simple environments, the
     * context class loader will find resources from the class path. In environments where different
     * threads can have different class loaders, for example app servers, the context class loader
     * will typically have been set to an appropriate loader for the current thread.
     *
     * <p>In the unusual case where the context class loader is null, the class loader that loaded
     * this class ({@code Resources}) will be used instead.
     *
     * @throws IllegalArgumentException if the resource is not found
     */
    public static URL getResource(String resourceName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader loader = contextClassLoader == null ? Resources.class.getClassLoader() : contextClassLoader;
        final URL url = loader.getResource(resourceName);
        if (url == null) {
            throw new IllegalArgumentException("resource " + resourceName + " not found.");
        }
        return url;
    }

    /**
     * Reads all bytes from a URL into a byte array.
     *
     * @param url the URL to read from
     * @return a byte array containing all the bytes from the URL
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return ByteStreams.toByteArray(inputStream);
        }
    }

    /**
     * Reads all characters from a URL into a {@link String}, using the given character set.
     *
     * @param url     the URL to read from
     * @param charset the charset used to decode the input stream; see {@link java.nio.charset.StandardCharsets} for helpful
     *                predefined constants
     * @return a string containing all the characters from the URL
     * @throws IOException if an I/O error occurs.
     */
    public static String toString(URL url, Charset charset) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return CharStreams.toString(new InputStreamReader(inputStream, charset));
        }
    }

    /**
     * Copies all bytes from a URL to an output stream.
     *
     * @param from the URL to read from
     * @param to the output stream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(URL from, OutputStream to) throws IOException {
        try (InputStream inputStream = from.openStream()) {
            ByteStreams.copy(inputStream, to);
        }
    }
}
