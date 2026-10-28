package io.dropwizard.logging.common;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

/**
 * A {@link ThrowableHandlingConverter} that delegates to a provided {@link Converter} and will truncate converted
 * results to ensure that the returned result is no longer than the specified character limit.
 */
public class TruncatingThrowableHandlingConverter extends ThrowableHandlingConverter {
    private static final String ELLIPSIS = "…"; // single-char ellipsis (U+2026)
    private final int characterLimit;
    private final ThrowableHandlingConverter delegate;

    public TruncatingThrowableHandlingConverter(final ThrowableHandlingConverter delegate, final int characterLimit) {
        this.characterLimit = characterLimit;
        this.delegate = delegate;
    }

    @Override
    public void start() {
        delegate.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        delegate.stop();
    }

    @Override
    public String convert(ILoggingEvent event) {
        return truncateIfNeeded(delegate.convert(event), characterLimit);
    }

    /**
     * Truncates the input string so the result is no longer than charLimit. When result is truncated, it ends with
     * an ellipsis character.
     */
    private static String truncateIfNeeded(final String input, final int charLimit) {
        // no limit defined, no truncation needed
        if (charLimit <= 0) {
            return input;
        }

        // input is less than the limit, no truncation needed
        if (input == null || input.length() <= charLimit) {
            return input;
        }

        // reserve room for the ellipsis
        int ellipsisLength = ELLIPSIS.length();

        // returns the longest prefix of input whose length is <= charLimit}, without splitting a surrogate pair.
        // This does not handle grapheme clusters.
        int end = charLimit - ellipsisLength;
        // if we'd split a surrogate pair, back off by one char
        if (end > 0 && Character.isLowSurrogate(input.charAt(end))) {
            end--;
        }
        return input.substring(0, end) + ELLIPSIS;
    }
}
