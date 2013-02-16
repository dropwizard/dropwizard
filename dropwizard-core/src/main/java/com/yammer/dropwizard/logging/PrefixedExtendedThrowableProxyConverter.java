package com.yammer.dropwizard.logging;

import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;

public class PrefixedExtendedThrowableProxyConverter extends PrefixedThrowableProxyConverter {

    @Override
    protected void extraData(StringBuilder builder, StackTraceElementProxy step) {
        if (step != null) {
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
    }
}
