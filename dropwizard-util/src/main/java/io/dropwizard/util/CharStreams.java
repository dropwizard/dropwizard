package io.dropwizard.util;

import java.io.IOException;
import java.io.Reader;

/**
 * Provides helper methods to work with character streams.
 * @since 2.0
 */
@Deprecated
public final class CharStreams {

    private CharStreams() {
    }

    /**
     * Constructs a string from the contents of the given {@link Reader}.
     *
     * @param reader the source {@link Reader}
     * @return a string representing the concatenation of the chars of the {@link Reader}
     * @throws IOException If an I/O error occurs
     */
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
