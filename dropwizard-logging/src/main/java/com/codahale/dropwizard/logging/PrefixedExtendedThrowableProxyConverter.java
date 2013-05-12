package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;

/**
 * A {@link PrefixedThrowableProxyConverter} which prefixes stack traces with {@code !}.
 */
public class PrefixedExtendedThrowableProxyConverter extends PrefixedThrowableProxyConverter {
    @Override
    protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
        if (step != null) {
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
    }
}
