package io.dropwizard.logging;

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

    private static volatile PatternFactory defaultPatternFactory = new PatternFactory();

    public DropwizardLayout(LoggerContext context, TimeZone timeZone) {
        super();
        setOutputPatternAsHeader(false);
        getDefaultConverterMap().put("ex", PrefixedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("xEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("rEx", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
        setPattern(defaultPatternFactory.build(timeZone));
        setContext(context);
    }

    /**
     * Sets the factory that provides the default log format pattern for <em>new</em> instances
     * of {@link DropwizardLayout}.  This allows an application to define its own default log
     * format pattern, but patterns explicitly set in the configuration YAML file will always
     * override this behavior.  Applications must invoke this method <em>before anything
     * initializes the logging subsystem</em>; a good place is early in the {@code main} method.
     *
     * @param factory pattern factory instance that has already been initialized and configured
     */
    public static void setDefaultPattern(PatternFactory factory) {
        defaultPatternFactory = factory;
    }

    static PatternFactory getDefaultPattern() {
        return defaultPatternFactory;
    }

    /** Provides methods for generating Logback-compatible format patterns. */
    public static class PatternFactory {
        /**
         * Generates a log format based on the provided {@code TimeZone}.
         * Implementations of this method are expected to be thread-safe.
         *
         * @param timeZone the time zone configured for the target log appender
         * @return a Logback-compatible format pattern
         */
        public String build(TimeZone timeZone) {
            return "%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%rEx";
        }
    }
}
