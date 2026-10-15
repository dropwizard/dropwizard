package io.dropwizard.logging.json.layout;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.dropwizard.logging.json.EventAttribute;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds JSON messages from logging events of the type {@link ILoggingEvent}.
 */
public class EventJsonLayout extends AbstractJsonLayout<ILoggingEvent> {

    private Set<EventAttribute> includes;

    @Nullable
    private String jsonProtocolVersion;

    private final ThrowableHandlingConverter throwableProxyConverter;
    private final TimestampFormatter timestampFormatter;
    private final Map<String, Object> additionalFields;
    private final Map<String, String> customFieldNames;

    private Set<String> includesMdcKeys;
    private final boolean flattenMdc;

    public EventJsonLayout(JsonFormatter jsonFormatter, TimestampFormatter timestampFormatter,
                           ThrowableHandlingConverter throwableProxyConverter, Set<EventAttribute> includes,
                           Map<String, String> customFieldNames, Map<String, Object> additionalFields,
                           Set<String> includesMdcKeys, boolean flattenMdc) {
        super(jsonFormatter);
        this.timestampFormatter = timestampFormatter;
        this.additionalFields = new HashMap<>(additionalFields);
        this.customFieldNames = new HashMap<>(customFieldNames);
        this.throwableProxyConverter = throwableProxyConverter;
        this.includes = new HashSet<>(includes);
        this.includesMdcKeys = new HashSet<>(includesMdcKeys);
        this.flattenMdc = flattenMdc;
    }

    @Override
    public void start() {
        throwableProxyConverter.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        throwableProxyConverter.stop();
    }

    @Override
    protected Map<String, Object> toJsonMap(ILoggingEvent event) {
        final MapBuilder mapBuilder = new MapBuilder(timestampFormatter, customFieldNames, additionalFields, includes.size())
            .addTimestamp("timestamp", isIncluded(EventAttribute.TIMESTAMP), event.getTimeStamp())
            .add("level", isIncluded(EventAttribute.LEVEL), () -> String.valueOf(event.getLevel()))
            .add("thread", isIncluded(EventAttribute.THREAD_NAME), event::getThreadName)
            .add("marker", isIncluded(EventAttribute.MARKER) && event.getMarker() != null, () -> event.getMarker().getName())
            .add("logger", isIncluded(EventAttribute.LOGGER_NAME), event::getLoggerName)
            .add("message", isIncluded(EventAttribute.MESSAGE), event::getFormattedMessage)
            .add("context", isIncluded(EventAttribute.CONTEXT_NAME), () -> event.getLoggerContextVO().getName())
            .add("version", jsonProtocolVersion != null, jsonProtocolVersion)
            .add("exception", isIncluded(EventAttribute.EXCEPTION) && event.getThrowableProxy() != null,
                () -> throwableProxyConverter.convert(event));

        final boolean includeMdc = isIncluded(EventAttribute.MDC);
        if (flattenMdc) {
            filterMdc(event.getMDCPropertyMap()).forEach((k,v) -> mapBuilder.add(k, includeMdc, v));
        } else {
            mapBuilder.addMap("mdc", includeMdc, () -> filterMdc(event.getMDCPropertyMap()));
        }

        final boolean includeCallerData = isIncluded(EventAttribute.CALLER_DATA);
        final StackTraceElement[] callerData = event.getCallerData();
        if (includeCallerData && callerData.length >= 1) {
            final StackTraceElement stackTraceElement = callerData[0];
            mapBuilder.add("caller_class_name", includeCallerData, stackTraceElement.getClassName());
            mapBuilder.add("caller_method_name", includeCallerData, stackTraceElement.getMethodName());
            mapBuilder.add("caller_file_name", includeCallerData, stackTraceElement.getFileName());
            mapBuilder.addNumber("caller_line_number", includeCallerData, stackTraceElement.getLineNumber());
        }

        return mapBuilder.build();
    }

    private Map<String, String> filterMdc(Map<String, String> mdcPropertyMap) {
        if (includesMdcKeys.isEmpty()) {
            return mdcPropertyMap;
        }
        return mdcPropertyMap.entrySet()
            .stream()
            .filter(e -> includesMdcKeys.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isIncluded(EventAttribute include) {
        return includes.contains(include);
    }

    public Set<EventAttribute> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<EventAttribute> includes) {
        this.includes = EnumSet.copyOf(includes);
    }

    @Nullable
    public String getJsonProtocolVersion() {
        return jsonProtocolVersion;
    }

    public void setJsonProtocolVersion(@Nullable String jsonProtocolVersion) {
        this.jsonProtocolVersion = jsonProtocolVersion;
    }

    public Set<String> getIncludesMdcKeys() {
        return includesMdcKeys;
    }

    public void setIncludesMdcKeys(Set<String> includesMdcKeys) {
        this.includesMdcKeys = new HashSet<>(includesMdcKeys);
    }
}
