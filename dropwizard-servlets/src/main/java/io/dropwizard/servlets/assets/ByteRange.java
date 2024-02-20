package io.dropwizard.servlets.assets;

import com.google.errorprone.annotations.Immutable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Immutable
public final class ByteRange {

    private final int start;
    private final int end;

    public ByteRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public static ByteRange parse(final String byteRange,
                                  final int resourceLength) {
        final String asciiString = new String(byteRange.getBytes(), StandardCharsets.US_ASCII);
        // missing separator
        if (!byteRange.contains("-")) {
            final int start = Integer.parseInt(asciiString);
            return new ByteRange(start, resourceLength - 1);
        }
        // negative range
        if (byteRange.indexOf("-") == 0) {
            final int start = Integer.parseInt(asciiString);
            return new ByteRange(resourceLength + start, resourceLength - 1);
        }
        final List<String> parts = Arrays.stream(asciiString.split("-", -1))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        final int start = Integer.parseInt(parts.get(0));
        if (parts.size() == 2) {
            int end = Integer.parseInt(parts.get(1));
            if (end > resourceLength) {
                end = resourceLength - 1;
            }
            return new ByteRange(start, end);
        } else {
            return new ByteRange(start, resourceLength - 1);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final ByteRange other = (ByteRange) obj;
        return start == other.start && end == other.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return String.format("%d-%d", start, end);
    }
}
