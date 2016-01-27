package io.dropwizard.request.logging;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;

import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.request.logging.async.AsyncAccessEventAppenderFactory;
import io.dropwizard.request.logging.layout.LogbackAccessRequestLayoutFactory;

/**
 * A factory for creating {@link LogbackAccessRequestLog} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>The set of {@link AppenderFactory appenders} to which requests will be logged.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("logback-access")
public class LogbackAccessRequestLogFactory implements RequestLogFactory {

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<IAccessEvent>> appenders = ImmutableList
            .<AppenderFactory<IAccessEvent>> of(new ConsoleAppenderFactory<>());

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

        final LogbackAccessRequestLog requestLog = new LogbackAccessRequestLog();

        final LevelFilterFactory<IAccessEvent> levelFilterFactory = new NullLevelFilterFactory<>();
        final AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory = new AsyncAccessEventAppenderFactory();
        final LayoutFactory<IAccessEvent> layoutFactory = new LogbackAccessRequestLayoutFactory();

        for (AppenderFactory<IAccessEvent> output : appenders) {
            requestLog.addAppender(output.build(context, name, layoutFactory, levelFilterFactory, asyncAppenderFactory));
        }

        return requestLog;
    }
}
