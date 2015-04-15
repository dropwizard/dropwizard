package io.dropwizard.jersey.validation;

import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.QueryParam;

public class BeanParameter {
    @QueryParam("name")
    @NotEmpty
    private String name;

    public String getName() {
        return name;
    }
}
