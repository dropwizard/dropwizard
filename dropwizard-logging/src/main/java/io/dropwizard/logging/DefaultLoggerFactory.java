package io.dropwizard.logging;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;

public class DefaultLoggerFactory {
    @NotNull
    private Level level = Level.INFO;

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory> appenders = ImmutableList
            .<AppenderFactory> of(new ConsoleAppenderFactory());

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public ImmutableList<AppenderFactory> getAppenders() {
        return appenders;
    }

    public void setAppenders(ImmutableList<AppenderFactory> appenders) {
        this.appenders = appenders;
    }

}
