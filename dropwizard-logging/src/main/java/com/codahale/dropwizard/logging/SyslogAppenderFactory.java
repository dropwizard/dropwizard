package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.net.SyslogConstants;
import com.codahale.dropwizard.validation.OneOf;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * An {@link AppenderFactory} implementation which provides an appender that sends events to a syslog server.
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
 *             The syslog facility to use. Can be either {@code auth}, {@code authpriv}, {@code daemon}, {@code cron},
 *             {@code ftp}, {@code lpr}, {@code kern}, {@code mail}, {@code news}, {@code syslog}, {@code user},
 *             {@code uucp}, {@code local0}, {@code local1}, {@code local2}, {@code local3}, {@code local4},
 *             {@code local5}, {@code local6}, or {@code local7}.
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
 * </table>
 */
@JsonTypeName("syslog")
public class SyslogAppenderFactory extends AbstractAppenderFactory {
    @NotNull
    private String host = "localhost";

    @Min(1)
    @Max(65535)
    private int port = SyslogConstants.SYSLOG_PORT;

    @NotNull
    @OneOf(
            value = {
                    "auth", "authpriv", "daemon", "cron", "ftp", "lpr", "kern", "mail", "news", "syslog", "user",
                    "uucp", "local0", "local1", "local2", "local3", "local4", "local5", "local6", "local7"
            },
            ignoreCase = true, ignoreWhitespace = true
    )
    private String facility = "local0";

    /**
     * Returns the hostname of the syslog server.
     */
    @JsonProperty
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname of the syslog server.
     */
    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the syslog facility to use.
     */
    @JsonProperty
    public String getFacility() {
        return facility;
    }

    /**
     * Sets the syslog facility to use.
     */
    @JsonProperty
    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * Returns the hostname of the syslog port.
     */
    @JsonProperty
    public int getPort() {
        return port;
    }

    /**
     * Sets the hostname of the syslog port.
     */
    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final SyslogAppender appender = new SyslogAppender();
        appender.setContext(context);
        appender.setSuffixPattern(logFormat);
        appender.setSyslogHost(host);
        appender.setPort(port);
        appender.setFacility(facility);
        addThresholdFilter(appender, threshold);
        appender.start();
        return appender;
    }
}
