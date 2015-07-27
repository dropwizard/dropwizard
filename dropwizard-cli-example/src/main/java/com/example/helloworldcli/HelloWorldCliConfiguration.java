package com.example.helloworldcli;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class HelloWorldCliConfiguration extends Configuration {

    @NotNull
    private String greeting;

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

}
