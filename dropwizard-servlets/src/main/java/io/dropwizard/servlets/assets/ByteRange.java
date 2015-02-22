package io.dropwizard.servlets.assets;

import javax.annotation.concurrent.Immutable;
import com.google.common.base.Objects;

@Immutable
public final class ByteRange {

    private final int start;
    private final int end;
    private final boolean hasEnd;
    
    public ByteRange(final int start) {
        this.start = start;
        this.end = -1;
        this.hasEnd = false;
    }
    
    public ByteRange(final int start, final int end) {
        this.start = start;
        this.end = end;
        this.hasEnd = true;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean hasEnd() {
        return hasEnd;
    }

    public static ByteRange parse(final String byteRange) throws NumberFormatException {
        // negative range or missing separator
        if (byteRange.indexOf("-") < 1) {
            final int start = Integer.parseInt(byteRange);
            return new ByteRange(start);
        }
        final String[] parts = byteRange.split("-");
        if (parts.length == 2) {
            final int start = Integer.parseInt(parts[0]);
            final int end = Integer.parseInt(parts[1]);
            return new ByteRange(start, end);
        }
        else {
            final int start = Integer.parseInt(parts[0]);
            return new ByteRange(start);
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
        return Objects.equal(start, other.start) && Objects.equal(end, other.end)
                && Objects.equal(hasEnd, other.hasEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(start, end, hasEnd);
    }

    @Override
    public String toString() {
        if (hasEnd) {
            return String.format("%d-%d", start, end);
        }
        else {
            return String.valueOf(start);
        }
    }
}
