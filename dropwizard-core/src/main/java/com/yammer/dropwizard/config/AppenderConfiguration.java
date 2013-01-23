package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yammer.dropwizard.logging.LogbackFactory;

import javax.validation.constraints.NotNull;

/**
 * Configuration for a logback appender.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AppenderConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @NotNull
    @JsonProperty
    private Level threshold = Level.ALL;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Level getThreshold() {
        return threshold;
    }

    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    public Appender<ILoggingEvent> buildAppender() {
        Appender<ILoggingEvent> appender = createAppender();
        LogbackFactory.addThresholdFilter(appender, threshold);
        appender.start();
        return appender;
    }

    protected abstract Appender<ILoggingEvent> createAppender();
}
