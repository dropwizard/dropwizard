package io.dropwizard.logging.json.layout;

import ch.qos.logback.access.spi.IAccessEvent;
import io.dropwizard.logging.json.AccessAttribute;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds JSON messages from access log events as {@link IAccessEvent}.
 */
public class AccessJsonLayout extends AbstractJsonLayout<IAccessEvent> {
    private static final String USER_AGENT = "User-Agent";

    private Set<AccessAttribute> includes;

    private SortedSet<String> requestHeaders = Collections.emptySortedSet();
    private boolean flattenRequestHeaders;
    private SortedSet<String> responseHeaders = Collections.emptySortedSet();
    private boolean flattenResponseHeaders;
    private SortedSet<String> requestAttributes = Collections.emptySortedSet();
    private boolean flattenRequestAttributes;

    @Nullable
    private String jsonProtocolVersion;

    private final TimestampFormatter timestampFormatter;
    private final Map<String, Object> additionalFields;
    private final Map<String, String> customFieldNames;

    public AccessJsonLayout(JsonFormatter jsonFormatter, TimestampFormatter timestampFormatter,
                            Set<AccessAttribute> includes, Map<String, String> customFieldNames,
                            Map<String, Object> additionalFields) {
        super(jsonFormatter);
        this.timestampFormatter = timestampFormatter;
        this.additionalFields = new HashMap<>(additionalFields);
        this.customFieldNames = new HashMap<>(customFieldNames);
        this.includes = EnumSet.copyOf(includes);
    }

    @Override
    protected Map<String, Object> toJsonMap(IAccessEvent event) {
        final MapBuilder mapBuilder = new MapBuilder(timestampFormatter, customFieldNames, additionalFields, includes.size())
            .addNumber("port", isIncluded(AccessAttribute.LOCAL_PORT), event::getLocalPort)
            .addNumber("contentLength", isIncluded(AccessAttribute.CONTENT_LENGTH), event::getContentLength)
            .addTimestamp("timestamp", isIncluded(AccessAttribute.TIMESTAMP), event.getTimeStamp())
            .add("method", isIncluded(AccessAttribute.METHOD), event::getMethod)
            .add("protocol", isIncluded(AccessAttribute.PROTOCOL), event::getProtocol)
            .add("requestContent", isIncluded(AccessAttribute.REQUEST_CONTENT), event::getRequestContent)
            .add("remoteAddress", isIncluded(AccessAttribute.REMOTE_ADDRESS), event::getRemoteAddr)
            .add("remoteUser", isIncluded(AccessAttribute.REMOTE_USER), event::getRemoteUser);
        addMapOrFlatten(mapBuilder, "headers", !requestHeaders.isEmpty(), () -> filterHeaders(event.getRequestHeaderMap(), requestHeaders), flattenRequestHeaders);
        mapBuilder
            .addMap("params", isIncluded(AccessAttribute.REQUEST_PARAMETERS), event::getRequestParameterMap)
            .addNumber("requestTime", isIncluded(AccessAttribute.REQUEST_TIME), event::getElapsedTime)
            .add("uri", isIncluded(AccessAttribute.REQUEST_URI), event::getRequestURI)
            .add("url", isIncluded(AccessAttribute.REQUEST_URL), event::getRequestURL)
            .add("pathQuery", isIncluded(AccessAttribute.PATH_QUERY), () -> event.getRequestURI() + event.getQueryString())
            .add("remoteHost", isIncluded(AccessAttribute.REMOTE_HOST), event::getRemoteHost)
            .add("responseContent", isIncluded(AccessAttribute.RESPONSE_CONTENT), event::getResponseContent);
        addMapOrFlatten(mapBuilder, "responseHeaders", !responseHeaders.isEmpty(), () -> filterHeaders(event.getResponseHeaderMap(), responseHeaders), flattenResponseHeaders);
        mapBuilder
            .add("serverName", isIncluded(AccessAttribute.SERVER_NAME), event::getServerName)
            .addNumber("status", isIncluded(AccessAttribute.STATUS_CODE), event::getStatusCode)
            .add("userAgent", isIncluded(AccessAttribute.USER_AGENT), () -> event.getRequestHeader(USER_AGENT))
            .add("version", jsonProtocolVersion != null, jsonProtocolVersion);
        addMapOrFlatten(mapBuilder, "requestAttributes", !requestAttributes.isEmpty(), () -> filterRequestAttributes(requestAttributes, event), flattenRequestAttributes);
        return mapBuilder.build();
    }

    private static void addMapOrFlatten(MapBuilder mapBuilder, String mapName, boolean hasContent, Supplier<Map<String,String>> mapGenerator, boolean flatten){
        if (flatten) {
            mapGenerator.get().forEach((k, v) -> mapBuilder.add(k, hasContent, v));
        } else {
            mapBuilder.addMap(mapName, hasContent, mapGenerator::get);
        }
    }

    private boolean isIncluded(AccessAttribute attribute) {
        return includes.contains(attribute);
    }

    private Map<String, String> filterRequestAttributes(Set<String> requestAttributeNames, IAccessEvent event) {
        return requestAttributeNames.stream()
            .filter(name -> event.getAttribute(name) != null)
            .collect(Collectors.toMap(Function.identity(), event::getAttribute));
    }

    private Map<String, String> filterHeaders(Map<String, String> headers, Set<String> filteredHeaderNames) {
        if (filteredHeaderNames.isEmpty()) {
            return Collections.emptyMap();
        }
        return headers.entrySet().stream()
            .filter(e -> filteredHeaderNames.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Set<AccessAttribute> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<AccessAttribute> includes) {
        this.includes = EnumSet.copyOf(includes);
    }

    @Nullable
    public String getJsonProtocolVersion() {
        return jsonProtocolVersion;
    }

    public void setJsonProtocolVersion(@Nullable String jsonProtocolVersion) {
        this.jsonProtocolVersion = jsonProtocolVersion;
    }

    public Set<String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Set<String> requestHeaders) {
        final TreeSet<String> headers = new TreeSet<>(String::compareToIgnoreCase);
        headers.addAll(requestHeaders);
        this.requestHeaders = headers;
    }

    /**
     * @since 2.0
     */
    public boolean isFlattenRequestHeaders() {
        return flattenRequestHeaders;
    }

    /**
     * @since 2.0
     */
    public void setFlattenRequestHeaders(boolean flattenRequestHeaders) {
        this.flattenRequestHeaders = flattenRequestHeaders;
    }

    public Set<String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Set<String> responseHeaders) {
        final TreeSet<String> headers = new TreeSet<>(String::compareToIgnoreCase);
        headers.addAll(responseHeaders);
        this.responseHeaders = headers;
    }

    /**
     * @since 2.0
     */
    public boolean isFlattenResponseHeaders() {
        return flattenResponseHeaders;
    }

    /**
     * @since 2.0
     */
    public void setFlattenResponseHeaders(boolean flattenResponseHeaders) {
        this.flattenResponseHeaders = flattenResponseHeaders;
    }

    /**
     * @since 2.0
     */
    public Set<String> getRequestAttributes() {
        return requestAttributes;
    }

    /**
     * @since 2.0
     */
    public void setRequestAttributes(Set<String> requestAttributes) {
        final TreeSet<String> attributes = new TreeSet<>(String::compareToIgnoreCase);
        attributes.addAll(requestAttributes);
        this.requestAttributes = attributes;
    }

    /**
     * @since 2.0
     */
    public boolean isFlattenRequestAttributes() {
        return flattenRequestAttributes;
    }

    /**
     * @since 2.0
     */
    public void setFlattenRequestAttributes(boolean flattenRequestAttributes) {
        this.flattenRequestAttributes = flattenRequestAttributes;
    }
}
