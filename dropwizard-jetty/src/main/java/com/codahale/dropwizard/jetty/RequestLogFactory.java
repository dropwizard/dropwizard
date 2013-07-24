package com.codahale.dropwizard.jetty;

import ch.qos.logback.access.PatternLayoutEncoder;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.dropwizard.logging.AccessAppenderFactory;
import com.codahale.dropwizard.logging.AppenderFactory;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * A factory for creating {@link RequestLog} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>UTC</td>
 *         <td>The time zone to which request timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code pattern}</td>
 *         <td>common</td>
 *         <td>The string defining the <a href="http://logback.qos.ch/manual/layouts.html#AccessPatternLayout">conversion pattern</a> to log access requests..</td>
 *     </tr>*
 *     <tr>
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>
 *             The set of {@link AppenderFactory appenders} to which requests will be logged.
 *         </td>
 *     </tr>
 * </table>
 */
public class RequestLogFactory {

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Valid
    @NotNull
    private ImmutableList<AccessAppenderFactory> appenders = ImmutableList.<AccessAppenderFactory>of(
            new ConsoleAppenderFactory()
    );

    @NotNull
    private String pattern = "common";

    @JsonProperty
    public ImmutableList<AccessAppenderFactory> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(ImmutableList<AccessAppenderFactory> appenders) {
        this.appenders = appenders;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return !appenders.isEmpty();
    }

    @JsonProperty
    public String getPattern() {
        return pattern;
    }

    @JsonProperty
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public RequestLog build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();
        context.reset();

        DropwizardRequestLog log = new DropwizardRequestLog();

        for (AccessAppenderFactory output : this.appenders) {
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern(pattern);
            encoder.start();

            log.addAppender(output.build(context, encoder));
        }

        return log;
    }
}
