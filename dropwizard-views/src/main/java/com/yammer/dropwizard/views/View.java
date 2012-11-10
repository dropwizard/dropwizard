package com.yammer.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

public abstract class View {
    private final String templateName;
    private final Timer renderingTimer;

    protected View(String templateName) {

        this.templateName = resolveName(templateName);
        this.renderingTimer = Metrics.defaultRegistry().newTimer(getClass(), "rendering");
    }

    private String resolveName(String templateName) {
        if (templateName.startsWith("/")) {
            return templateName;
        }
        final String packagePath = getClass().getPackage().getName().replace('.', '/');
        return String.format("/%s/%s", packagePath, templateName);
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
