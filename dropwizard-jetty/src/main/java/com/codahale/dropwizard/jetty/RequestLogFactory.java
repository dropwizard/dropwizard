package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.logging.ConsoleLoggingOutput;
import com.codahale.dropwizard.logging.LoggingOutput;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

public class RequestLogFactory {
    private static class RequestLogLayout extends LayoutBase<ILoggingEvent> {
        @Override
        public String doLayout(ILoggingEvent event) {
            return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
        }
    }

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Valid
    @NotNull
    private ImmutableList<LoggingOutput> outputs = ImmutableList.<LoggingOutput>of(
            new ConsoleLoggingOutput()
    );

    @JsonProperty
    public ImmutableList<LoggingOutput> getOutputs() {
        return outputs;
    }

    @JsonProperty
    public void setOutputs(ImmutableList<LoggingOutput> outputs) {
        this.outputs = outputs;
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
        return !outputs.isEmpty();
    }

    public RequestLogHandler build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);
        final LoggerContext context = logger.getLoggerContext();

        final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<>();

        final RequestLogLayout layout = new RequestLogLayout();
        layout.start();

        for (LoggingOutput output : outputs) {
            appenders.addAppender(output.build(context, name, layout));
        }

        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(appenders, timeZone));

        return handler;
    }
}
