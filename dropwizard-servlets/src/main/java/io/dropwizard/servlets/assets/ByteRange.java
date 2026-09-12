package io.dropwizard.servlets.assets;

import com.google.errorprone.annotations.Immutable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Immutable
public final class ByteRange {

    private final long start;
    private final long end;

    public ByteRange(final long start, final long end) {
        if (start < 0) {
            throw new IllegalArgumentException("start must be >= 0");
        }
        if (end < start) {
            throw new IllegalArgumentException("end must be >= start");
        }
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public static ByteRange parse(final String byteRange,
                                  final long resourceLength) {
        final String asciiString = new String(byteRange.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // missing separator
        if (!byteRange.contains("-")) {
            final long start = Long.parseLong(asciiString);
            return new ByteRange(start, resourceLength - 1);
        }
        // negative range
        if (byteRange.indexOf("-") == 0) {
            final long start = Long.parseLong(asciiString);
            final long calculatedStart = resourceLength + start;
            return new ByteRange(calculatedStart < 0 ? 0 : calculatedStart, resourceLength - 1);
        }
        final List<String> parts = Arrays.stream(asciiString.split("-", -1))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        final long start = Long.parseLong(parts.get(0));
        if (parts.size() == 2) {
            long end = Long.parseLong(parts.get(1));
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
