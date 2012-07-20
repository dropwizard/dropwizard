package com.yammer.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Locale;

/**
 * A logger class which provides SLF4J-style formatting without SLF4J's less-than-pleasant API.
 *
 * <code>
 *
 * // Explicit class specification
 * private static final Log LOG = Log.forClass(Thingy.class);
 *
 * // Or, automatic class specification
 * private static final Log LOG = Log.forThisClass();
 * ...
 *
 * LOG.debug("Simple usage: {} / {}", a, b);
 * LOG.debug("No need for manual arrays: {} - {} - {} - {}", a, b, c, d);
 * LOG.warn(exception, "Exceptions go first but don't prevent message formatting: {}", otherStuff);
 * </code>
 */
@SuppressWarnings("UnusedDeclaration")
public class Log {

	/**
	 * Returns a {@link Log} instance for the current class.
	 * The current class is determined in the static context by inspecting the stack.
	 * Further details about this approach may be found
	 * <a href="http://www.javaspecialists.eu/archive/Issue137.html">here</a>.
	 * 
	 * @return a {@link Log} instance with the current class name
	 */
	public static Log forThisClass() {
		Throwable t = new Throwable();
		StackTraceElement directCaller = t.getStackTrace()[1];
		return named(directCaller.getClassName());
	}

	/**
     * Returns a {@link Log} instance for the given class.
     *
     * @param klass    a given class
     * @return a {@link Log} instance with {@code klass}'s name
     */
    public static Log forClass(Class<?> klass) {
        // a thread-safe SLF4J initialization routine is apparently hard, so I get to do dumb
        // shit like this
        while (true) {
            final org.slf4j.Logger logger = LoggerFactory.getLogger(klass);
            if (logger instanceof Logger) {
                return forSlf4jLogger(logger);
            }
        }
    }

    /**
     * Returns a {@link Log} instance with the given name.
     *
     * @param name    a given name
     * @return a {@link Log} instance with the given name
     */
    public static Log named(String name) {
        // a thread-safe SLF4J initialization routine is apparently hard, so I get to do dumb
        // shit like this
        while (true) {
            final org.slf4j.Logger logger = LoggerFactory.getLogger(name);
            if (logger instanceof Logger) {
                return forSlf4jLogger(logger);
            }
        }
    }

    /**
     * Returns a {@link Log} instance with the same name as the given Log4j {@link Logger} instance.
     *
     * @param logger    a Log4j {@link Logger}
     * @return a {@link Log} instance with the same name as {@code logger}
     */
    public static Log forLog4jLogger(org.apache.log4j.Logger logger) {
        return named(logger.getName());
    }

    /**
     * Returns a {@link Log} instance with the same name as the given slf4j {@link org.slf4j.Logger}
     * instance.
     *
     * @param logger    an Slf4j {@link org.slf4j.Logger}
     * @return a {@link Log} instance with the same name as {@code logger}
     */
    public static Log forSlf4jLogger(org.slf4j.Logger logger) {
        return new Log((Logger) logger);
    }

    /**
     * Returns a {@link Log} instance with the same name as the given {@code java.util.logging}
     * {@link java.util.logging.Logger} instance.
     *
     * @param logger    a {@code java.util.logging} {@link java.util.logging.Logger} instance
     * @return a {@link Log} instance with the same name as {@code logger}
     */
    public static Log forJulLogger(java.util.logging.Logger logger) {
        return named(logger.getName());
    }
    
    private final Logger logger;

    private Log(Logger logger) {
        this.logger = logger;
    }

    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    @Deprecated
    public void setLevel(org.apache.log4j.Level level) {
        final String s = level.toString().toUpperCase(Locale.US);
        if ("FATAL".equals(s)) {
            logger.setLevel(Level.ERROR);
        }
        logger.setLevel(Level.toLevel(s));
    }

    // TRACE

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void trace(String message) {
        logger.trace(message);
    }

    public void trace(String message, Object arg) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.format(message, arg).getMessage());
        }
    }

    public void trace(String message, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.format(message, arg1, arg2).getMessage());
        }
    }

    public void trace(String message, Object... args) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    public void trace(Throwable e, String message, Object arg) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.format(message, arg).getMessage(), e);
        }
    }

    public void trace(Throwable e, String message, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
        }
    }

    public void trace(Throwable e, String message, Object... args) {
        if (isTraceEnabled()) {
            logger.trace(MessageFormatter.arrayFormat(message, args).getMessage(), e);
        }
    }

    // DEBUG

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Object arg) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.format(message, arg).getMessage());
        }
    }

    public void debug(String message, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.format(message, arg1, arg2).getMessage());
        }
    }

    public void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    public void debug(Throwable e, String message, Object arg) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.format(message, arg).getMessage(), e);
        }
    }

    public void debug(Throwable e, String message, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
        }
    }

    public void debug(Throwable e, String message, Object... args) {
        if (isDebugEnabled()) {
            logger.debug(MessageFormatter.arrayFormat(message, args).getMessage(), e);
        }
    }

    // INFO

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Object arg) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.format(message, arg).getMessage());
        }
    }

    public void info(String message, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.format(message, arg1, arg2).getMessage());
        }
    }

    public void info(String message, Object... args) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    public void info(Throwable e, String message, Object arg) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.format(message, arg).getMessage(), e);
        }
    }

    public void info(Throwable e, String message, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
        }
    }

    public void info(Throwable e, String message, Object... args) {
        if (isInfoEnabled()) {
            logger.info(MessageFormatter.arrayFormat(message, args).getMessage(), e);
        }
    }

    // WARN

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Object arg) {
        logger.warn(MessageFormatter.format(message, arg).getMessage());
    }

    public void warn(String message, Object arg1, Object arg2) {
        logger.warn(MessageFormatter.format(message, arg1, arg2).getMessage());
    }

    public void warn(String message, Object... args) {
        logger.warn(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    public void warn(Throwable e, String message, Object arg) {
        logger.warn(MessageFormatter.format(message, arg).getMessage(), e);
    }

    public void warn(Throwable e, String message, Object arg1, Object arg2) {
        logger.warn(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
    }

    public void warn(Throwable e, String message, Object... args) {
        logger.warn(MessageFormatter.arrayFormat(message, args).getMessage(), e);
    }

    // ERROR

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Object arg) {
        logger.error(MessageFormatter.format(message, arg).getMessage());
    }

    public void error(String message, Object arg1, Object arg2) {
        logger.error(MessageFormatter.format(message, arg1, arg2).getMessage());
    }

    public void error(String message, Object... args) {
        logger.error(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    public void error(Throwable e, String message, Object arg) {
        logger.error(MessageFormatter.format(message, arg).getMessage(), e);
    }

    public void error(Throwable e, String message, Object arg1, Object arg2) {
        logger.error(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
    }

    public void error(Throwable e, String message, Object... args) {
        logger.error(MessageFormatter.arrayFormat(message, args).getMessage(), e);
    }

    // FATAL

    @Deprecated
    public void fatal(String message) {
        logger.error(message);
    }

    @Deprecated
    public void fatal(String message, Object arg) {
        logger.error(MessageFormatter.format(message, arg).getMessage());
    }

    @Deprecated
    public void fatal(String message, Object arg1, Object arg2) {
        logger.error(MessageFormatter.format(message, arg1, arg2).getMessage());
    }

    @Deprecated
    public void fatal(String message, Object... args) {
        logger.error(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Deprecated
    public void fatal(Throwable e, String message, Object arg) {
        logger.error(MessageFormatter.format(message, arg).getMessage(), e);
    }

    @Deprecated
    public void fatal(Throwable e, String message, Object arg1, Object arg2) {
        logger.error(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
    }

    @Deprecated
    public void fatal(Throwable e, String message, Object... args) {
        logger.error(MessageFormatter.arrayFormat(message, args).getMessage(), e);
    }

    public Level getLevel() {
        return logger.getEffectiveLevel();
    }
}
