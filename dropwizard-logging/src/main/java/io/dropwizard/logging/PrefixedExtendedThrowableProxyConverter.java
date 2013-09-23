package io.dropwizard.logging;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;

/**
 * An {@link ExtendedThrowableProxyConverter} which prefixes stack traces with {@code !}.
 */
public class PrefixedExtendedThrowableProxyConverter extends PrefixedThrowableProxyConverter {
    @Override
    protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
        if (step != null) {
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
    }
}
