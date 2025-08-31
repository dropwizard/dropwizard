package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;

import java.util.TimeZone;

/**
 * A base layout for Dropwizard.
 * <ul>
 *     <li>Disables pattern headers.</li>
 *     <li>Prefixes logged exceptions with {@code !}.</li>
 *     <li>Sets the pattern to the given timezone.</li>
 * </ul>
 */
public class DropwizardLayout extends PatternLayout {
    public DropwizardLayout(LoggerContext context, TimeZone timeZone) {
        super();
        setOutputPatternAsHeader(false);
        getDefaultConverterSupplierMap().put("dwEx", PrefixedThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwException", PrefixedThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwThrowable", PrefixedThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwREx", PrefixedRootCauseFirstThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwRootException", PrefixedRootCauseFirstThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwXEx", PrefixedExtendedThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwXException", PrefixedExtendedThrowableProxyConverter::new);
        getDefaultConverterSupplierMap().put("dwXThrowable", PrefixedExtendedThrowableProxyConverter::new);
        setPattern("%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%dwREx");
        setContext(context);
    }
}
