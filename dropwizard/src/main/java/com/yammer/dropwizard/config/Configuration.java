package com.yammer.dropwizard.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

// TODO: 11/7/11 <coda> -- document Configuration
// TODO: 11/7/11 <coda> -- test Configuration

public class Configuration {
    @NotNull
    @Valid
    private HttpConfiguration http = new HttpConfiguration();

    @NotNull
    @Valid
    private LoggingConfiguration logging = new LoggingConfiguration();

    public HttpConfiguration getHttpConfiguration() {
        return http;
    }

    public LoggingConfiguration getLoggingConfiguration() {
        return logging;
    }
}
