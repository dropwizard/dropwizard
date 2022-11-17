package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;

import java.util.Map;
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
        Map<String, String> defaultConverterMap = getDefaultConverterMap();
        defaultConverterMap.put("dwEx", PrefixedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwException", PrefixedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwThrowable", PrefixedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwREx", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwRootException", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwXEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwXException", PrefixedExtendedThrowableProxyConverter.class.getName());
        defaultConverterMap.put("dwXThrowable", PrefixedExtendedThrowableProxyConverter.class.getName());
        setPattern("%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%dwREx");
        setContext(context);
    }
}
