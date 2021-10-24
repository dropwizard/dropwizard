package io.dropwizard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @since 2.0
 */
public final class ByteStreams {

    private ByteStreams() {
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        copyInternal(in, to);
        return to.toByteArray();
    }

    /**
     * @deprecated this is an internal method to dropwizard-util. Consider apache-commons instead.
     */
    @Deprecated
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
