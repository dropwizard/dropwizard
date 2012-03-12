package com.yammer.dropwizard.views;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

public abstract class View {
    private final String templateName;
    private final Timer renderingTimer;

    protected View(String templateName) {
        this.templateName = templateName;
        this.renderingTimer = Metrics.newTimer(getClass(), "rendering");
    }

    public String getTemplateName() {
        return templateName;
    }

    Timer getRenderingTimer() {
        return renderingTimer;
    }
}
