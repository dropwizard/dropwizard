package com.example.helloworld;

import com.example.helloworld.core.Template;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.constraints.NotNull;

public class HelloWorldConfiguration extends Configuration {
    @NotNull
    private String template;
    
    @NotNull
    private String defaultName = "Stranger";

    public String getTemplate() {
        return template;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Template buildTemplate() {
        return new Template(template, defaultName);
    }
}
