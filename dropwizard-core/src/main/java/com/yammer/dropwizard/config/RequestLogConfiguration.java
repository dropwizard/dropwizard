package com.yammer.dropwizard.config;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public class RequestLogConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @NotNull
    @JsonProperty
    private String filenamePattern = "./logs/yyyy_mm_dd.log";

    @Min(1)
    @Max(50)
    @JsonProperty
    private int retainedFileCount = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public String getFilenamePattern() {
        return filenamePattern;
    }

    public int getRetainedFileCount() {
        return retainedFileCount;
    }
}
