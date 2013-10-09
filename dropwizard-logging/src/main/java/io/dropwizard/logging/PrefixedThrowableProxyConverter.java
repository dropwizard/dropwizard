package io.dropwizard.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

import java.util.regex.Pattern;

/**
 * A {@link ThrowableProxyConverter} which prefixes stack traces with {@code !}.
 */
public class PrefixedThrowableProxyConverter extends ThrowableProxyConverter {

    static final Pattern PATTERN = Pattern.compile("^\\t?", Pattern.MULTILINE);
    static final String PREFIX = "! ";

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        return PATTERN.matcher(super.throwableProxyToString(tp)).replaceAll(PREFIX);
    }
}
