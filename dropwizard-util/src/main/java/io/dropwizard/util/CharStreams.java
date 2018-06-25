package io.dropwizard.util;

import java.io.IOException;
import java.io.Reader;

public final class CharStreams {

    private CharStreams() {
    }

    public static String toString(Reader reader) throws IOException {
        final StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int length;
        while ((length = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }
}
