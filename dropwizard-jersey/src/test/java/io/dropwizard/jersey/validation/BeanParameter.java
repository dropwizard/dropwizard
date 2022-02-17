package io.dropwizard.jersey.validation;

import io.dropwizard.validation.ValidationMethod;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;

public class BeanParameter {
    @QueryParam("name")
    @NotEmpty
    private String name = "";

    public String getName() {
        return name;
    }

    @QueryParam("choice")
    @NotNull
    @SuppressWarnings("NullAway.Init")
    private Choice choice;

    public Choice getChoice() {
        return choice;
    }

    @ValidationMethod(message = "name must be Coda")
    public boolean isCoda() {
        return "Coda".equals(name);
    }
}
