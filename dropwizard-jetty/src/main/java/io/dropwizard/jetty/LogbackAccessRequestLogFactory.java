package io.dropwizard.jetty;

import java.util.Optional;
import java.util.TimeZone;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Layout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;

import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.AsyncAppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.filter.NullFilterFactory;

/**
 * A factory for creating {@link RequestLog} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code timeZone}</td>
 * <td>UTC</td>
 * <td>The time zone to which request timestamps will be converted, if using the default {@code logFormat}.</td>
 * </tr>
 * <tr>
 * <td>{@code logFormat}</td>
 * <td>HTTP [%t{dd/MMM/yyyy:HH:mm:ss Z,UTC}] %h %l %u "%r" %s %b "%i{Referer}" "%i{User-Agent}"</td>
 * <td>The Logback pattern with which events will be formatted. See
 * <a href="http://logback.qos.ch/manual/layouts.html#logback-access">the Logback documentation</a> for details.</td>
 * </tr>
 * <tr>
 * <td>{@code appenders}</td>
 * <td>a default {@link ConsoleAppenderFactory console} appender</td>
 * <td>The set of {@link AppenderFactory appenders} to which requests will be logged.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("logback-access")
public class LogbackAccessRequestLogFactory implements RequestLogFactory {

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<IAccessEvent>> appenders = ImmutableList
            .<AppenderFactory<IAccessEvent>> of(new ConsoleAppenderFactory<IAccessEvent>());

    @JsonProperty
    public ImmutableList<AppenderFactory<IAccessEvent>> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(ImmutableList<AppenderFactory<IAccessEvent>> appenders) {
        this.appenders = appenders;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return !appenders.isEmpty();
    }

    @Override
    public RequestLog build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();

        final DropwizardRequestLog requestLog = new DropwizardRequestLog();

        final FilterFactory<IAccessEvent> thresholdFilterFactory = new NullFilterFactory<>();
        final AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory = new AsyncAccessEventAppenderFactory();

        for (AppenderFactory<IAccessEvent> output : appenders) {
            final Layout<IAccessEvent> layout = new DropwizardRequestLayout(context, output.getLogFormat());
            layout.start();
            requestLog.addAppender(output.build(context, name, layout, thresholdFilterFactory, asyncAppenderFactory));
        }

        return requestLog;
    }
}
