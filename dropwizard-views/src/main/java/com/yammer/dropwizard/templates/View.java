package com.yammer.dropwizard.templates;

public abstract class View {
    private final String templateName;

    protected View(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
