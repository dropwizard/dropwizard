package io.dropwizard.logging.json;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.json.layout.AccessJsonLayout;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td >{@code includes}</td>
 * <td>(timestamp, remoteAddress,requestTime, requestUri, statusCode, method, protocol, contentLength, userAgent))</td>
 * <td>Set of logging event attributes to include in the JSON map.</td>
 * </tr>
 * <tr>
 * <td>{@code requestHeaders}</td>
 * <td >(empty)</td>
 * <td>Set of request headers included in the JSON map as the ``headers`` field.</td>
 * </tr>
 * <tr>
 * <td>{@code responseHeaders}</td>
 * <td>(empty)</td>
 * <td>Set of response headers included in the JSON map as the ``responseHeaders`` field.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("access-json")
public class AccessJsonLayoutBaseFactory extends AbstractJsonLayoutBaseFactory<IAccessEvent> {
    private EnumSet<AccessAttribute> includes = EnumSet.of(
            AccessAttribute.REMOTE_ADDRESS,
            AccessAttribute.REMOTE_USER,
            AccessAttribute.REQUEST_TIME,
            AccessAttribute.REQUEST_URI,
            AccessAttribute.STATUS_CODE,
            AccessAttribute.METHOD,
            AccessAttribute.PROTOCOL,
            AccessAttribute.CONTENT_LENGTH,
            AccessAttribute.USER_AGENT,
            AccessAttribute.TIMESTAMP);

    private Set<String> responseHeaders = Collections.emptySet();
    private boolean flattenResponseHeaders;
    private Set<String> requestHeaders = Collections.emptySet();
    private boolean flattenRequestHeaders;
    private Set<String> requestAttributes = Collections.emptySet();
    private boolean flattenRequestAttributes;

    @JsonProperty
    public Set<String> getResponseHeaders() {
        return responseHeaders;
    }

    @JsonProperty
    public void setResponseHeaders(Set<String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean isFlattenResponseHeaders() {
        return flattenResponseHeaders;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setFlattenResponseHeaders(boolean flattenResponseHeaders) {
        this.flattenResponseHeaders = flattenResponseHeaders;
    }

    @JsonProperty
    public Set<String> getRequestHeaders() {
        return requestHeaders;
    }

    @JsonProperty
    public void setRequestHeaders(Set<String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean isFlattenRequestHeaders() {
        return flattenRequestHeaders;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setFlattenRequestHeaders(boolean flattenRequestHeaders) {
        this.flattenRequestHeaders = flattenRequestHeaders;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public Set<String> getRequestAttributes() {
        return requestAttributes;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setRequestAttributes(Set<String> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean isFlattenRequestAttributes() {
        return flattenRequestAttributes;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setFlattenRequestAttributes(boolean flattenRequestAttributes) {
        this.flattenRequestAttributes = flattenRequestAttributes;
    }

    @JsonProperty
    public EnumSet<AccessAttribute> getIncludes() {
        return includes;
    }

    @JsonProperty
    public void setIncludes(EnumSet<AccessAttribute> includes) {
        this.includes = includes;
    }

    @Override
    public LayoutBase<IAccessEvent> build(LoggerContext context, TimeZone timeZone) {
        final AccessJsonLayout jsonLayout = new AccessJsonLayout(
                createDropwizardJsonFormatter(),
                createTimestampFormatter(timeZone),
                includes,
                getCustomFieldNames(),
                getAdditionalFields());
        jsonLayout.setContext(context);
        jsonLayout.setRequestHeaders(requestHeaders);
        jsonLayout.setResponseHeaders(responseHeaders);
        jsonLayout.setRequestAttributes(requestAttributes);
        jsonLayout.setFlattenRequestHeaders(flattenRequestHeaders);
        jsonLayout.setFlattenResponseHeaders(flattenResponseHeaders);
        jsonLayout.setFlattenRequestAttributes(flattenRequestAttributes);
        return jsonLayout;
    }
}
