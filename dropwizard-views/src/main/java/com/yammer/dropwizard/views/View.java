package com.yammer.dropwizard.views;

public abstract class View {
    private final String templateName;

    protected View(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
