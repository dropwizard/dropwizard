package com.yammer.dropwizard.logging;

import org.apache.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * A logger class which provides SLF4J-style formatting without SLF4J's less-than-pleasant API.
 *
 * <code>
 * private static final Log LOG = Log.forClass(Thingy.class);
 *
 * ...
 *
 * LOG.debug("Simple usage: {} / {}", a, b);
 * LOG.debug("No need for manual arrays: {} - {} - {} - {}", a, b, c, d);
 * LOG.warn(exception, "Exceptions go first but don't prevent message formatting: {}", otherStuff);
 * </code>
 */
public class Log {
    public static Log forClass(Class<?> klass) {
        return new Log(Logger.getLogger(klass));
    }
    
    private Logger logger;

    private Log(Logger logger) {
        this.logger = logger;
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
            logger.trace(MessageFormatter.format(message, arg).getMessage());
        }
    }

    public void debug(String message, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            logger.trace(MessageFormatter.format(message, arg1, arg2).getMessage());
        }
    }

    public void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            logger.trace(MessageFormatter.arrayFormat(message, args).getMessage());
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

    public void fatal(String message) {
        logger.fatal(message);
    }

    public void fatal(String message, Object arg) {
        logger.fatal(MessageFormatter.format(message, arg).getMessage());
    }

    public void fatal(String message, Object arg1, Object arg2) {
        logger.fatal(MessageFormatter.format(message, arg1, arg2).getMessage());
    }

    public void fatal(String message, Object... args) {
        logger.fatal(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    public void fatal(Throwable e, String message, Object arg) {
        logger.fatal(MessageFormatter.format(message, arg).getMessage(), e);
    }

    public void fatal(Throwable e, String message, Object arg1, Object arg2) {
        logger.fatal(MessageFormatter.format(message, arg1, arg2).getMessage(), e);
    }

    public void fatal(Throwable e, String message, Object... args) {
        logger.fatal(MessageFormatter.arrayFormat(message, args).getMessage(), e);
    }
}
