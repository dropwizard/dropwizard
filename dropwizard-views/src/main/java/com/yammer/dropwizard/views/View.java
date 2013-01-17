package com.yammer.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

import java.nio.charset.Charset;

public abstract class View {
    private final String templateName;
    private final Charset charset;
    private final Timer renderingTimer;

    protected View(String templateName) {
        this(templateName, null);
    }

    protected View(String templateName, Charset charset) {
        this.templateName = resolveName(templateName);
        this.charset = charset;
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
    public Optional<Charset> getCharset() {
        return charset != null ? Optional.of(charset) : Optional.<Charset>absent();
    }

    @JsonIgnore
    Timer getRenderingTimer() {
        return renderingTimer;
    }
}
