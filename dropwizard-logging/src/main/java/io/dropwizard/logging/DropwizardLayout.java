package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;

/**
 * A base layout for Dropwizard.
 * <ul>
 *     <li>Disables pattern headers.</li>
 *     <li>Prefixes logged exceptions with {@code !}.</li>
 * </ul>
 */
public class DropwizardLayout extends PatternLayout {
    public DropwizardLayout(LoggerContext context, String pattern) {
        setOutputPatternAsHeader(false);

        getDefaultConverterMap().put("ex", PrefixedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("xEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("rEx", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
        setPattern(pattern);
        setContext(context);
    }
}
