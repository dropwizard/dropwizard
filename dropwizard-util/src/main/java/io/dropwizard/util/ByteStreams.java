package io.dropwizard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides helper methods for easier conversions of streams.
 * @since 2.0
 *
 * @deprecated this class exists for compatibility with Java 8 and will be removed in Dropwizard 3.0.
 */
@Deprecated
public final class ByteStreams {

    private ByteStreams() {
    }

    /**
     * Converts an {@link InputStream} to a {@code byte[]}.
     *
     * @param in the {@link InputStream} to convert
     * @return the {@code byte[]} representing the contents of the {@link InputStream}
     * @throws IOException If an I/O error occurs
     * @deprecated For users of Java 11+, consider {@link InputStream#readAllBytes()} instead.
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        copyInternal(in, to);
        return to.toByteArray();
    }

    /**
     * Copies the contents from an {@link InputStream} to an {@link OutputStream}.
     *
     * @param in the source stream
     * @param to the target stream
     * @throws IOException If an I/O error occurs
     * @deprecated this is an internal method to dropwizard-util. Consider apache-commons instead.
     */
    public static void copy(InputStream in, OutputStream to) throws IOException {
        copyInternal(in, to);
    }

    static void copyInternal(InputStream in, OutputStream to) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
    }
}
