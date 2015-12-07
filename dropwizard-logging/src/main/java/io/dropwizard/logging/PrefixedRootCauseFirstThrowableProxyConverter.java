package io.dropwizard.logging;

import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

import java.util.regex.Pattern;

import static io.dropwizard.logging.PrefixedThrowableProxyConverter.PREFIX;
import static io.dropwizard.logging.PrefixedThrowableProxyConverter.PATTERN;

/**
 * A {@link RootCauseFirstThrowableProxyConverter} that prefixes stack traces with {@code !}.
 */
public class PrefixedRootCauseFirstThrowableProxyConverter
        extends RootCauseFirstThrowableProxyConverter {

    private static final String CAUSING = PREFIX + "Causing:";
    private static final Pattern CAUSING_PATTERN = Pattern.compile("^" + Pattern.quote(PREFIX) + "Wrapped by:",
            Pattern.MULTILINE);

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        final String prefixed = PATTERN.matcher(super.throwableProxyToString(tp)).replaceAll(PREFIX);
        return CAUSING_PATTERN.matcher(prefixed).replaceAll(CAUSING);
    }
}
