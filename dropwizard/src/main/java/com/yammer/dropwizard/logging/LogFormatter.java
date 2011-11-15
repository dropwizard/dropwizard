package com.yammer.dropwizard.logging;

// TODO: 10/12/11 <coda> -- test LogFormatter
// TODO: 10/12/11 <coda> -- document LogFormatter

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.spi.LoggingEvent;

public class LogFormatter extends EnhancedPatternLayout {
    private static final String STACK_TRACE_PREFIX = "! ";
    private static final char NEWLINE = '\n';

    public LogFormatter() {
        super("%-5p [%d{ISO8601}{UTC}] %c: %m\n");
    }

    @Override
    public String format(LoggingEvent event) {
        final StringBuilder line = new StringBuilder(200);
        line.append(super.format(event));
        if (event.getThrowableInformation() != null) {
            event.getThrowableInformation().getThrowableStrRep();
            for (int i = 0; i < event.getThrowableInformation().getThrowableStrRep().length; i++) {
                final String element = event.getThrowableInformation().getThrowableStrRep()[i];
                line.append(STACK_TRACE_PREFIX).append(element).append(NEWLINE);
            }
        }
        return line.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }
}
