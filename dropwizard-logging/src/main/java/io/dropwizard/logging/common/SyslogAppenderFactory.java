package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.net.SyslogConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.async.AsyncAppenderFactory;
import io.dropwizard.logging.common.filter.LevelFilterFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link AppenderFactory} implementation which provides an appender that sends events to a
 * syslog server.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code host}</td>
 *         <td>{@code localhost}</td>
 *         <td>The hostname of the syslog server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code port}</td>
 *         <td>{@code 514}</td>
 *         <td>The port on which the syslog server is listening.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code facility}</td>
 *         <td>{@code local0}</td>
 *         <td>
 *             The syslog facility to use. Can be either {@code auth}, {@code authpriv},
 *             {@code daemon}, {@code cron}, {@code ftp}, {@code lpr}, {@code kern}, {@code mail},
 *             {@code news}, {@code syslog}, {@code user}, {@code uucp}, {@code local0},
 *             {@code local1}, {@code local2}, {@code local3}, {@code local4}, {@code local5},
 *             {@code local6}, or {@code local7}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to write to the file.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code logFormat}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code includeStackTrace}</td>
 *         <td>include stack traces in log messages</td>
 *         <td>
 *             Setting {@code includeStackTrace} to {@code false} causes no Throwable's stack trace data to be sent to the syslog daemon.
 *             By default, stack trace data is sent to syslog daemon.
 *             See <a href="http://logback.qos.ch/apidocs/ch/qos/logback/classic/net/SyslogAppender.html#setThrowableExcluded(boolean)">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("syslog")
public class SyslogAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {
    public enum Facility {
        ALERT,
        AUDIT,
        AUTH,
        AUTHPRIV,
        DAEMON,
        CLOCK,
        CRON,
        FTP,
        LPR,
        KERN,
        MAIL,
        NEWS,
        NTP,
        SYSLOG,
        USER,
        UUCP,
        LOCAL0,
        LOCAL1,
        LOCAL2,
        LOCAL3,
        LOCAL4,
        LOCAL5,
        LOCAL6,
        LOCAL7
    }

    private static final String LOG_TOKEN_NAME = "%app";
    private static final String LOG_TOKEN_PID = "%pid";

    private static final Pattern PID_PATTERN = Pattern.compile("(\\d+)@");
    private static String pid = "";

    // make an attempt to get the PID of the process
    // this will only work on UNIX platforms; for others, the PID will be "unknown"
    static {
        final Matcher matcher = PID_PATTERN.matcher(ManagementFactory.getRuntimeMXBean().getName());
        if (matcher.find()) {
            pid = "[" + matcher.group(1) + "]";
        }
    }

    @NotNull
    private String host = "localhost";

    @Min(1)
    @Max(65535)
    private int port = SyslogConstants.SYSLOG_PORT;

    @NotNull
    private Facility facility = Facility.LOCAL0;

    // PrefixedThrowableProxyConverter does not apply to syslog appenders, as stack traces are sent separately from
    // the main message. This means that the standard prefix of `!` is not used for syslog
    @NotNull
    private String stackTracePrefix = SyslogAppender.DEFAULT_STACKTRACE_PATTERN;

    private boolean includeStackTrace = true;

    public SyslogAppenderFactory() {
        // prefix the logFormat with the application name and PID (if available)
        this.logFormat = LOG_TOKEN_NAME + LOG_TOKEN_PID + ": " + SyslogAppender.DEFAULT_SUFFIX_PATTERN;
    }

    /**
     * Returns the hostname of the syslog server.
     */
    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public Facility getFacility() {
        return facility;
    }

    @JsonProperty
    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public boolean getIncludeStackTrace() {
        return includeStackTrace;
    }

    @JsonProperty
    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }

    @JsonProperty
    public String getStackTracePrefix() {
        return stackTracePrefix;
    }

    @JsonProperty
    public void setStackTracePrefix(String stackTracePrefix) {
        this.stackTracePrefix = stackTracePrefix;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, LayoutFactory<ILoggingEvent> layoutFactory,
                                         LevelFilterFactory<ILoggingEvent> levelFilterFactory, AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        final SyslogAppender appender = new SyslogAppender();
        appender.setName("syslog-appender");
        appender.setContext(context);
        if (logFormat != null && !logFormat.isEmpty()) {
            appender.setSuffixPattern(logFormat
                    .replace(LOG_TOKEN_PID, pid)
                    .replace(LOG_TOKEN_NAME, Matcher.quoteReplacement(applicationName)));
        }
        appender.setSyslogHost(host);
        appender.setPort(port);
        appender.setFacility(facility.toString().toLowerCase(Locale.ENGLISH));
        appender.setThrowableExcluded(!includeStackTrace);
        appender.setStackTracePattern(stackTracePrefix);
        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();
        return wrapAsync(appender, asyncAppenderFactory);
    }
}
