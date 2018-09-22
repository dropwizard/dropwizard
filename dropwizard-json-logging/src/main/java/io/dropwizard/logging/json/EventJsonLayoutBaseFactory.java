package io.dropwizard.logging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.json.layout.EventJsonLayout;

import io.dropwizard.logging.json.layout.ExceptionFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.annotation.Nullable;

/**
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@code includes}</td>
 * <td>(level, threadName, mdc, loggerName, message, exception, timestamp)</td>
 * <td>Set of logging event attributes to include in the JSON map.</td>
 * </tr>
 * <tr>
 * <td>{@code includesMdcKeys}</td>
 * <td>(empty)</td>
 * <td>Set of MDC keys which should be included in the JSON map. By default includes everything.</td>
 * </tr>
 * <tr>
 * <td>{@code flattenMdc}</td>
 * <td>{@code false}</td>
 * <td>Whether the MDC should be included under the key "mdc" or flattened into the map.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("json")
public class EventJsonLayoutBaseFactory extends AbstractJsonLayoutBaseFactory<ILoggingEvent> {

    private EnumSet<EventAttribute> includes = EnumSet.of(EventAttribute.LEVEL,
        EventAttribute.THREAD_NAME, EventAttribute.MDC, EventAttribute.LOGGER_NAME, EventAttribute.MESSAGE,
        EventAttribute.EXCEPTION, EventAttribute.TIMESTAMP);

    private Set<String> includesMdcKeys = Collections.emptySet();
    private boolean flattenMdc = false;

    @Nullable
    private ExceptionFormat exceptionFormat;

    @JsonProperty
    public EnumSet<EventAttribute> getIncludes() {
        return includes;
    }

    @JsonProperty
    public void setIncludes(EnumSet<EventAttribute> includes) {
        this.includes = includes;
    }

    @JsonProperty
    public Set<String> getIncludesMdcKeys() {
        return includesMdcKeys;
    }

    @JsonProperty
    public void setIncludesMdcKeys(Set<String> includesMdcKeys) {
        this.includesMdcKeys = includesMdcKeys;
    }

    @JsonProperty
    public boolean isFlattenMdc() {
        return flattenMdc;
    }

    @JsonProperty
    public void setFlattenMdc(boolean flattenMdc) {
        this.flattenMdc = flattenMdc;
    }

    @JsonProperty("exception")
    public void setExceptionFormat(ExceptionFormat exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
    }

    @JsonProperty("exception")
    @Nullable
    public ExceptionFormat getExceptionFormat() {
        return exceptionFormat;
    }

    @Override
    public LayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        final EventJsonLayout jsonLayout = new EventJsonLayout(createDropwizardJsonFormatter(),
            createTimestampFormatter(timeZone), createThrowableProxyConverter(context), includes, getCustomFieldNames(),
            getAdditionalFields(), includesMdcKeys, flattenMdc);
        jsonLayout.setContext(context);
        return jsonLayout;
    }

    protected ThrowableHandlingConverter createThrowableProxyConverter(LoggerContext context) {
        if (exceptionFormat == null) {
            return new RootCauseFirstThrowableProxyConverter();
        }

        ThrowableHandlingConverter throwableHandlingConverter;
        if (exceptionFormat.isRootFirst()) {
            throwableHandlingConverter = new RootCauseFirstThrowableProxyConverter();
        } else {
            throwableHandlingConverter = new ExtendedThrowableProxyConverter();
        }

        List<String> options = new ArrayList<>();
        // depth must be added first
        options.add(exceptionFormat.getDepth());
        options.addAll(exceptionFormat.getEvaluators());

        throwableHandlingConverter.setOptionList(options);
        throwableHandlingConverter.setContext(context);

        return throwableHandlingConverter;
    }

}
