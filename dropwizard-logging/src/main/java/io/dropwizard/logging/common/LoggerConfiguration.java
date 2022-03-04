package io.dropwizard.logging.common;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Individual {@link Logger} configuration
 */
public class LoggerConfiguration {

    @NotNull
    private String level = "INFO";

    @Valid
    @NotNull
    private List<AppenderFactory<ILoggingEvent>> appenders = Collections.emptyList();

    private boolean additive = true;

    public boolean isAdditive() {
        return additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    public void setAppenders(List<AppenderFactory<ILoggingEvent>> appenders) {
        this.appenders = new ArrayList<>(appenders);
    }
}
