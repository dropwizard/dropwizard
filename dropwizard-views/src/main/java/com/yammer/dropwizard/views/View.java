package com.yammer.dropwizard.views;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class View {
    private final String templateName;
    private final Timer renderingTimer;

    protected View(String templateName) {
        this.templateName = templateName;
        this.renderingTimer = Metrics.newTimer(getClass(), "rendering");
    }

    @JsonIgnore
    public String getTemplateName() {
        return templateName;
    }

    @JsonIgnore
    Timer getRenderingTimer() {
        return renderingTimer;
    }
}
