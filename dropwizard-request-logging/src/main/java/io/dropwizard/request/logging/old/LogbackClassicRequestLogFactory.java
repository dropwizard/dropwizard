package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.JsonLayoutBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A factory for creating {@link RequestLog} instances using logback-classic.
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
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>
 *             The set of {@link AppenderFactory appenders} to which requests will be logged.
 *         </td>
 *     </tr>
 * </table>
 */
@JsonTypeName("classic")
public class LogbackClassicRequestLogFactory implements RequestLogFactory {

    private static class RequestLogLayoutFactory implements LayoutFactory<ILoggingEvent> {

        @Override
        public PatternLayoutBase<ILoggingEvent> buildPatternLayout(LoggerContext context, TimeZone timeZone) {
            return new RequestLogLayout(context);
        }

        @Override
        public JsonLayoutBase<ILoggingEvent> buildJsonLayout(LoggerContext context, TimeZone timeZone,
                                                             String timestampFormat,
                                                             boolean includeStackTrace, boolean prettyPrint) {
            return new RequestLogJsonLayout();
        }
    }

    private static class RequestLogJsonLayout extends JsonLayoutBase<ILoggingEvent> {

        @Override
        protected Map toJsonMap(ILoggingEvent event) {
            return Collections.singletonMap("message", event.getFormattedMessage());
        }
    }

    private static class RequestLogLayout extends PatternLayoutBase<ILoggingEvent> {

        private RequestLogLayout(Context context) {
            super();
            setContext(context);
        }

        @Override
        public String doLayout(ILoggingEvent event) {
            return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
        }

        @Override
        public Map<String, String> getDefaultConverterMap() {
            return ImmutableMap.of();
        }
    }

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<ILoggingEvent>> appenders = ImmutableList.of(
        new ConsoleAppenderFactory<ILoggingEvent>()
    );

    @JsonProperty
    public ImmutableList<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(ImmutableList<AppenderFactory<ILoggingEvent>> appenders) {
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
    @Override
    public boolean isEnabled() {
        return !appenders.isEmpty();
    }

    @Override
    public RequestLog build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();
        final LevelFilterFactory<ILoggingEvent> levelFilterFactory = new NullLevelFilterFactory<>();
        final AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();
        final LayoutFactory<ILoggingEvent> layoutFactory = new RequestLogLayoutFactory();
        final AppenderAttachableImpl<ILoggingEvent> attachable = new AppenderAttachableImpl<>();
        for (AppenderFactory<ILoggingEvent> appender : appenders) {
            attachable.addAppender(appender.build(context, name, layoutFactory, levelFilterFactory, asyncAppenderFactory));
        }

        return new DropwizardSlf4jRequestLog(attachable, timeZone);
    }
}
