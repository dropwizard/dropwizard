package com.codahale.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;

import java.nio.charset.Charset;

public abstract class View {
    private final String templateName;
    private final Charset charset;

    protected View(String templateName) {
        this(templateName, null);
    }

    protected View(String templateName, Charset charset) {
        this.templateName = resolveName(templateName);
        this.charset = charset;
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
        return Optional.fromNullable(charset);
    }
}
