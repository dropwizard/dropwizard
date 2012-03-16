package com.yammer.dropwizard.logging;

// TODO: 10/12/11 <coda> -- test LoggingBean
// TODO: 10/12/11 <coda> -- document LoggingBean

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;

public class LoggingBean implements LoggingMXBean {
    @Override
    public String getLoggerLevel(String loggerName) {
        return Log.named(loggerName).getLevel().toString();
    }

    @Override
    public List<String> getLoggerNames() {
        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        final List<String> names = Lists.newArrayList();
        for (Logger logger : root.getLoggerContext().getLoggerList()) {
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
        Log.named(loggerName).setLevel(newLevel);
    }

    @Override
    public String getParentLoggerName(String loggerName) {
        throw new UnsupportedOperationException("Can't determine parents.");
    }
}
