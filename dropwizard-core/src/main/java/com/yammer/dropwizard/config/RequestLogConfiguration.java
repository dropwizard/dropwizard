package com.yammer.dropwizard.config;

import com.yammer.dropwizard.validation.ValidationMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public class RequestLogConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @Min(1)
    @Max(50)
    @JsonProperty
    private int archivedFileCount = 5;

    @JsonProperty
    private String currentLogFilename;

    @JsonProperty
    private String archivedLogFilenamePattern;

    @NotNull
    @JsonProperty
    private TimeZone timeZone = LoggingConfiguration.UTC;

    @ValidationMethod(message = "must have a http.requestLog.currentLogFilename and " +
            "http.requestLog.archivedLogFilenamePattern if http.requestLog.enabled is true")
    public boolean isConfigured() {
        return !enabled || ((currentLogFilename != null) && (archivedLogFilenamePattern != null));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCurrentLogFilename() {
        return currentLogFilename;
    }

    public int getArchivedFileCount() {
        return archivedFileCount;
    }

    public String getArchivedLogFilenamePattern() {
        return archivedLogFilenamePattern;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
