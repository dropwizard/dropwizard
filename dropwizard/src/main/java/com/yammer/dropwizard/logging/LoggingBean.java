package com.yammer.dropwizard.logging;

// TODO: 10/12/11 <coda> -- test LoggingBean
// TODO: 10/12/11 <coda> -- document LoggingBean

import com.google.common.collect.Lists;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;

public class LoggingBean implements LoggingMXBean {
    @Override
    public String getLoggerLevel(String loggerName) {
        return Logger.getLogger(loggerName).getLevel().toString();
    }

    @Override
    public List<String> getLoggerNames() {
        final List<String> names = Lists.newArrayList();
        final Enumeration<?> loggers = Logger.getRootLogger()
                                             .getLoggerRepository()
                                             .getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            final Logger logger = (Logger) loggers.nextElement();
            names.add(logger.getName());
        }

        final Enumeration<String> moreNames = LogManager.getLogManager()
                                                        .getLoggerNames();
        while (moreNames.hasMoreElements()) {
            final String name = moreNames.nextElement();
            names.add(name);
        }

        Collections.sort(names);
        return names;
    }

    @Override
    public void setLoggerLevel(String loggerName, String levelName) {
        final Level newLevel = Level.toLevel(levelName, Level.INFO);
        Logger.getLogger(loggerName).setLevel(newLevel);
    }

    @Override
    public String getParentLoggerName(String loggerName) {
        final Category parent = Logger.getLogger(loggerName).getParent();
        if (parent != null) {
            return parent.getName();
        }
        return null;
    }
}
