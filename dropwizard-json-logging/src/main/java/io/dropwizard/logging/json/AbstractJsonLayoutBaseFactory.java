package io.dropwizard.logging.json;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.json.layout.JsonFormatter;
import io.dropwizard.logging.json.layout.TimestampFormatter;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.TimeZone;

/**
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@code timestampFormat}</td>
 * <td>(none)</td>
 * <td>By default, the timestamp is not formatted; To format the timestamp using set the property with the
 * corresponding {@link java.time.format.DateTimeFormatter} string, for example, {@code yyyy-MM-ddTHH:mm:ss.SSSZ}</td>
 * </tr>
 * <tr>
 * <td>{@code prettyPrint}</td>
 * <td>{@code false}</td>
 * <td>Whether the JSON output should be formatted for human readability.</td>
 * </tr>
 * <tr>
 * <td>{@code appendLineSeparator}</td>
 * <td>{@code true}</td>
 * <td>Whether to append a line separator at the end of the message formatted as JSON.</td>
 * </tr>
 * <tr>
 * <td>{@code customFieldNames}</td>
 * <td>empty</td>
 * <td>A map of field name replacements. For example:
 * <i>(requestTime:request_time, userAgent:user_agent)</i></td>
 * </tr>
 * <tr>
 * <td>{@code additionalFields}</td>
 * <td>empty</td>
 * <td>A map of fields to add.</td>
 * </tr>
 * </table>
 */
public abstract class AbstractJsonLayoutBaseFactory<E extends DeferredProcessingAware>
    implements DiscoverableLayoutFactory<E> {

    @Nullable
    private String timestampFormat;

    private boolean prettyPrint;
    private boolean appendLineSeparator = true;

    @NotNull
    private Map<String, String> customFieldNames = ImmutableMap.of();

    @NotNull
    private Map<String, Object> additionalFields = ImmutableMap.of();

    @JsonProperty
    @Nullable
    public String getTimestampFormat() {
        return timestampFormat;
    }

    @JsonProperty
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @JsonProperty
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    @JsonProperty
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @JsonProperty
    public boolean isAppendLineSeparator() {
        return appendLineSeparator;
    }

    @JsonProperty
    public void setAppendLineSeparator(boolean appendLineSeparator) {
        this.appendLineSeparator = appendLineSeparator;
    }

    @JsonProperty
    public Map<String, String> getCustomFieldNames() {
        return customFieldNames;
    }

    @JsonProperty
    public void setCustomFieldNames(Map<String, String> customFieldNames) {
        this.customFieldNames = customFieldNames;
    }

    @JsonProperty
    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    @JsonProperty
    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }

    protected JsonFormatter createDropwizardJsonFormatter() {
        return new JsonFormatter(Jackson.newObjectMapper(), isPrettyPrint(), isAppendLineSeparator());
    }

    protected TimestampFormatter createTimestampFormatter(TimeZone timeZone) {
        return new TimestampFormatter(getTimestampFormat(), timeZone.toZoneId());
    }
}
