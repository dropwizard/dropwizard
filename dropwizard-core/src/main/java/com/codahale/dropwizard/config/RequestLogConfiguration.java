package com.codahale.dropwizard.config;

import com.codahale.dropwizard.logging.ConsoleLoggingOutput;
import com.codahale.dropwizard.logging.LoggingOutput;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

import static com.codahale.dropwizard.config.LoggingConfiguration.UTC;

public class RequestLogConfiguration {
    @NotNull
    @JsonProperty
    private TimeZone timeZone = UTC;

    @Valid
    @NotNull
    @JsonProperty
    private ImmutableList<LoggingOutput> outputs = ImmutableList.<LoggingOutput>of(
            new ConsoleLoggingOutput()
    );

    public ImmutableList<LoggingOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ImmutableList<LoggingOutput> outputs) {
        this.outputs = outputs;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
