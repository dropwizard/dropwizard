package com.yammer.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.TimeZone;

@JsonTypeName("syslog")
public class SyslogLoggingOutput implements LoggingOutput {
    public enum Facility {
        AUTH, AUTHPRIV, DAEMON, CRON, FTP, LPR, KERN, MAIL, NEWS, SYSLOG, USER, UUCP,
        LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7;

        @Override
        @JsonValue
        public String toString() {
            return super.toString().replace("_", "+").toLowerCase(Locale.ENGLISH);
        }

        @JsonCreator
        public static Facility parse(String facility) {
            return valueOf(facility.toUpperCase(Locale.ENGLISH).replace('+', '_'));
        }
    }

    @NotNull
    @JsonProperty
    private Level threshold = Level.ALL;

    @NotNull
    @JsonProperty
    private String host = "localhost";

    @NotNull
    @JsonProperty
    private Facility facility = Facility.LOCAL0;

    @NotNull
    @JsonProperty
    private TimeZone timeZone = UTC;

    @JsonProperty
    private String logFormat;

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String serviceName) {
        final SyslogFormatter layout = new SyslogFormatter(context, timeZone, serviceName);
        layout.setOutputPatternAsHeader(false);
        layout.setContext(context);

        if (!Strings.isNullOrEmpty(logFormat)) {
            layout.setPattern(logFormat);
        }
        layout.start();

        final SyslogAppender appender = new SyslogAppender();
        appender.setContext(context);
        appender.setLayout(layout);
        appender.setSyslogHost(host);
        appender.setFacility(facility.toString());
        addThresholdFilter(appender, threshold);
        appender.start();

        return appender;
    }

    private void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }
}
