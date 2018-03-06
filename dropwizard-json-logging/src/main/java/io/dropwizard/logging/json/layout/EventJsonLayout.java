package io.dropwizard.logging.json.layout;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.dropwizard.logging.json.EventAttribute;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds JSON messages from logging events of the type {@link ILoggingEvent}.
 */
public class EventJsonLayout extends AbstractJsonLayout<ILoggingEvent> {

    private ImmutableSet<EventAttribute> includes;

    @Nullable
    private String jsonProtocolVersion;

    private final ThrowableHandlingConverter throwableProxyConverter;
    private final TimestampFormatter timestampFormatter;
    private final Map<String, Object> additionalFields;
    private final Map<String, String> customFieldNames;

    private ImmutableSet<String> includesMdcKeys;
    private final boolean flattenMdc;

    public EventJsonLayout(JsonFormatter jsonFormatter, TimestampFormatter timestampFormatter,
                           ThrowableHandlingConverter throwableProxyConverter, Set<EventAttribute> includes,
                           Map<String, String> customFieldNames, Map<String, Object> additionalFields,
                           Set<String> includesMdcKeys, boolean flattenMdc) {
        super(jsonFormatter);
        this.timestampFormatter = timestampFormatter;
        this.additionalFields = ImmutableMap.copyOf(additionalFields);
        this.customFieldNames = ImmutableMap.copyOf(customFieldNames);
        this.throwableProxyConverter = throwableProxyConverter;
        this.includes = ImmutableSet.copyOf(includes);
        this.includesMdcKeys = ImmutableSet.copyOf(includesMdcKeys);
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
        final MapBuilder mapBuilder = new MapBuilder(timestampFormatter, customFieldNames, additionalFields, 16)
            .addTimestamp("timestamp", isIncluded(EventAttribute.TIMESTAMP), event.getTimeStamp())
            .add("level", isIncluded(EventAttribute.LEVEL), String.valueOf(event.getLevel()))
            .add("thread", isIncluded(EventAttribute.THREAD_NAME), event.getThreadName())
            .add("logger", isIncluded(EventAttribute.LOGGER_NAME), event.getLoggerName())
            .add("message", isIncluded(EventAttribute.MESSAGE), event.getFormattedMessage())
            .add("context", isIncluded(EventAttribute.CONTEXT_NAME), event.getLoggerContextVO().getName())
            .add("version", jsonProtocolVersion != null, jsonProtocolVersion)
            .add("exception", isIncluded(EventAttribute.EXCEPTION) && event.getThrowableProxy() != null,
                 throwableProxyConverter.convert(event));
        final boolean includeMdc = isIncluded(EventAttribute.MDC);
        if (flattenMdc) {
            filterMdc(event.getMDCPropertyMap()).forEach((k,v) -> mapBuilder.add(k, includeMdc, v));
        } else {
            mapBuilder.add("mdc", includeMdc, filterMdc(event.getMDCPropertyMap()));
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

    private boolean isIncluded(EventAttribute exception) {
        return includes.contains(exception);
    }

    public ImmutableSet<EventAttribute> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<EventAttribute> includes) {
        this.includes = Sets.immutableEnumSet(includes);
    }

    @Nullable
    public String getJsonProtocolVersion() {
        return jsonProtocolVersion;
    }

    public void setJsonProtocolVersion(@Nullable String jsonProtocolVersion) {
        this.jsonProtocolVersion = jsonProtocolVersion;
    }

    public ImmutableSet<String> getIncludesMdcKeys() {
        return includesMdcKeys;
    }

    public void setIncludesMdcKeys(Set<String> includesMdcKeys) {
        this.includesMdcKeys = ImmutableSet.copyOf(includesMdcKeys);
    }
}
