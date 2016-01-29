package io.dropwizard.logging;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableList;
import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.Level;

import java.util.List;

/**
 * Individual {@link Logger} configuration
 */
public class LoggerConfiguration {

    @NotNull
    private Level level = Level.INFO;

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<ILoggingEvent>> appenders = ImmutableList.of();

    private boolean additive = true;

    public boolean isAdditive() {
        return additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public ImmutableList<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    public void setAppenders(List<AppenderFactory<ILoggingEvent>> appenders) {
        this.appenders = ImmutableList.copyOf(appenders);
    }
}
