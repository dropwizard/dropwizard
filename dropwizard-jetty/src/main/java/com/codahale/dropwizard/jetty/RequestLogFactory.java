package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.logging.AppenderFactory;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
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
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>
 *             The set of {@link AppenderFactory appenders} to which requests will be logged.
 *         </td>
 *     </tr>
 * </table>
 */
public class RequestLogFactory {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RequestLogFactory.class);

    private static class RequestLogLayout extends LayoutBase<ILoggingEvent> {
        @Override
        public String doLayout(ILoggingEvent event) {
            return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
        }
    }

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    private String alternateLogClass = null;

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory> appenders = ImmutableList.<AppenderFactory>of(
            new ConsoleAppenderFactory()
    );

    @JsonProperty
    public ImmutableList<AppenderFactory> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(ImmutableList<AppenderFactory> appenders) {
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

    @JsonProperty
    public String getAlternateLogClass() {
      return alternateLogClass;
    }

    @JsonProperty
    public void setAlternateLogClass( String alternateLogClass ) {
      this.alternateLogClass = alternateLogClass;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return !appenders.isEmpty();
    }

    public RequestLog build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();

        final RequestLogLayout layout = new RequestLogLayout();
        layout.start();

        final AppenderAttachableImpl<ILoggingEvent> attachable = new AppenderAttachableImpl<>();
        for (AppenderFactory output : this.appenders) {
            attachable.addAppender(output.build(context, name, layout));
        }

        return constructRequestLog( attachable, timeZone );
    }

    /**
     * Constructs the appropriate request log.
     *
     * If @{code alternateLogClass} is set, will load class and use reflection to instantiate the class. This assumes the alternate class
     * extends @{code AbstractRequestLog} and has the same constructor signature.
     *
     * If @{code alternateLogClass} is <em>NOT</em> set, it will instantiate a @{code Slf4jRequestLog} through normal means.
     *
     * @param appenders log appenders
     * @param timeZone log timezone
     * @return an AbstractRequestLog based on the configuration provided
     */
    @SuppressWarnings("unchecked" )
    private AbstractRequestLog constructRequestLog( AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone ) {
        if ( getAlternateLogClass() == null ) {
            return constructSlf4jRequestLog( appenders, timeZone );
        }
        try {
            Class alternateClass = Class.forName( getAlternateLogClass() );
            Constructor constructor = alternateClass.getDeclaredConstructor( AppenderAttachableImpl.class, TimeZone.class );
            AbstractRequestLog requestLog = (AbstractRequestLog) constructor.newInstance( appenders, timeZone );
            return requestLog;
        }
        catch (Exception ex ) {
            LOGGER.warn( "Error constructing {}. Falling back to default {}.", getAlternateLogClass(), Slf4jRequestLog.class.getName(), ex );
            return constructSlf4jRequestLog( appenders, timeZone );
        }
    }

    private AbstractRequestLog constructSlf4jRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone) {
       return new Slf4jRequestLog( appenders, timeZone );
    }

}
