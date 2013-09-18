package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * A {@link ThrowableProxyConverter} which prefixes stack traces with {@code !}.
 */
public class PrefixedThrowableProxyConverter extends ThrowableProxyConverter {
    private static final String PREFIX = "! ";

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        final StringBuilder buf = new StringBuilder(32);
        IThrowableProxy currentThrowable = tp;
        while (currentThrowable != null) {
            subjoinThrowableProxy(buf, currentThrowable);
            currentThrowable = currentThrowable.getCause();
        }
        return buf.toString();
    }

    void subjoinThrowableProxy(StringBuilder buf, IThrowableProxy tp) {
        subjoinFirstLine(buf, tp);


        buf.append(CoreConstants.LINE_SEPARATOR);
        final StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        final int commonFrames = tp.getCommonFrames();

        int maxIndex = stepArray.length;
        if (commonFrames > 0) {
            maxIndex -= commonFrames;
        }

        for (int i = 0; i < maxIndex; i++) {
            final String string = stepArray[i].toString();
            buf.append(PREFIX);
            buf.append(string);
            extraData(buf, stepArray[i]); // allow other data to be added
            buf.append(CoreConstants.LINE_SEPARATOR);
        }

        if (commonFrames > 0) {
            buf.append("!... ").append(tp.getCommonFrames()).append(
                    " common frames omitted").append(CoreConstants.LINE_SEPARATOR);
        }
    }

    private void subjoinFirstLine(StringBuilder buf, IThrowableProxy tp) {
        final int commonFrames = tp.getCommonFrames();
        if (commonFrames > 0) {
            buf.append(CoreConstants.CAUSED_BY);
        }
        subjoinExceptionMessage(buf, tp);
    }

    private void subjoinExceptionMessage(StringBuilder buf, IThrowableProxy tp) {
        buf.append(PREFIX).append(tp.getClassName()).append(": ").append(tp.getMessage());
    }
}
