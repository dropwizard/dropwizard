package com.yammer.dropwizard.templates;

public abstract class View<T> {
    private final String templateName;
    private final T model;

    protected View(String templateName, T model) {
        this.templateName = templateName;
        this.model = model;
    }

    public String getTemplateName() {
        return templateName;
    }

    public T getModel() {
        return model;
    }
}
