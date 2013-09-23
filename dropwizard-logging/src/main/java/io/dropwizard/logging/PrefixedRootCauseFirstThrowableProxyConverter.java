package io.dropwizard.logging;

import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * A {@link RootCauseFirstThrowableProxyConverter} that prefixes stack traces with {@code !}.
 */
public class PrefixedRootCauseFirstThrowableProxyConverter
        extends RootCauseFirstThrowableProxyConverter {

    private static final String PREFIX = "! ";

    private int maxDepth = Integer.MAX_VALUE;

    @Override
    public void start() {
        super.start();

        final String depth = getFirstOption();
        if (null == depth || "full".equalsIgnoreCase(depth)) {
            maxDepth = Integer.MAX_VALUE;
        } else if ("short".equalsIgnoreCase(depth)) {
            maxDepth = 2;
        } else {
            try {
                maxDepth = Integer.parseInt(depth) + 1;
            } catch (NumberFormatException e) {
                addError("Could not parse [" + depth + "] as an integer");
                maxDepth = Integer.MAX_VALUE;
            }
        }
    }

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        StringBuilder buf = new StringBuilder(2048);
        subjoinRootCauseFirst(tp, buf);
        return buf.toString();
    }

    private void subjoinRootCauseFirst(IThrowableProxy tp, StringBuilder buf) {
        if (tp.getCause() != null)
            subjoinRootCauseFirst(tp.getCause(), buf);
        subjoinRootCause(tp, buf);
    }

    private void subjoinRootCause(IThrowableProxy tp, StringBuilder buf) {
        subjoinFirstLineRootCauseFirst(buf, tp);
        buf.append(CoreConstants.LINE_SEPARATOR);
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        int commonFrames = tp.getCommonFrames();

        boolean unrestrictedPrinting = maxDepth > stepArray.length;


        int maxIndex = (unrestrictedPrinting) ? stepArray.length : maxDepth;
        if (commonFrames > 0 && unrestrictedPrinting) {
            maxIndex -= commonFrames;
        }

        for (int i = 0; i < maxIndex; i++) {
            String string = stepArray[i].toString();
            buf.append(PREFIX);
            buf.append(string);
            extraData(buf, stepArray[i]); // allow other data to be added
            buf.append(CoreConstants.LINE_SEPARATOR);
        }
    }

    private static void subjoinFirstLineRootCauseFirst(StringBuilder buf, IThrowableProxy tp) {
        buf.append(PREFIX);
        if (tp.getCause() != null) {
            buf.append("Causing: ");
        }

        buf.append(tp.getClassName()).append(": ").append(tp.getMessage());
    }
}
