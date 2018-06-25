package io.dropwizard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ByteStreams {

    private ByteStreams() {
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        copy(in, to);
        return to.toByteArray();
    }

    public static void copy(InputStream in, OutputStream to) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
    }
}
