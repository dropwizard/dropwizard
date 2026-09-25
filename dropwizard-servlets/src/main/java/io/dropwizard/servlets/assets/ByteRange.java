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

    /**
     * Parses a byte-range specification (from an HTTP {@code Range} header) into a {@link ByteRange}.
     *
     * <p>All arithmetic is performed with {@code long} values to avoid integer overflow when
     * request-supplied offsets are close to {@link Integer#MAX_VALUE}. Ranges that fall outside
     * {@code [0, resourceLength)} are clamped or rejected so that callers always receive a
     * semantically valid range.
     *
     * @param byteRange      the raw range string, e.g. {@code "0-499"}, {@code "-500"}
     * @param resourceLength the total number of bytes in the resource
     * @return a validated {@link ByteRange}
     * @throws NumberFormatException if the range string cannot be parsed as numbers
     */
    public static ByteRange parse(final String byteRange,
                                  final long resourceLength) {
        final String asciiString = new String(byteRange.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // suffix-range: no start position, count from end (e.g. "-500")
        if (byteRange.indexOf("-") == 0) {
            final long suffixLength = Long.parseLong(asciiString);
            // suffixLength is negative because the string starts with '-'
            final long start = Math.max(0L, resourceLength + suffixLength);
            return new ByteRange(start, resourceLength - 1);
        }
        // missing separator — treat the value as a plain start offset
        if (!byteRange.contains("-")) {
            final long start = Long.parseLong(asciiString);
            return new ByteRange(start, resourceLength - 1);
        }
        final List<String> parts = Arrays.stream(asciiString.split("-", -1))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        final long start = Long.parseLong(parts.get(0));
        final long end;
        if (parts.size() == 2) {
            long rawEnd = Long.parseLong(parts.get(1));
            // Clamp end to the last valid byte index
            end = Math.min(rawEnd, resourceLength - 1);
        } else {
            end = resourceLength - 1;
        }
        return new ByteRange(start, end);
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
