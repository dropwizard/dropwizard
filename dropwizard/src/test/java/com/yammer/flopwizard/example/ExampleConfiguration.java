package com.yammer.flopwizard.example;

import com.yammer.dropwizard.config.Configuration;

import javax.validation.constraints.NotNull;

public class ExampleConfiguration extends Configuration {
    @NotNull
    private String saying;

    public String getSaying() {
        return saying;
    }
}
