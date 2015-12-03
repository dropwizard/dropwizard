package io.dropwizard.jersey.validation;

import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.QueryParam;

public class BeanParameter {
    @QueryParam("name")
    @NotEmpty
    private String name;

    public String getName() {
        return name;
    }

    @ValidationMethod(message="name must be Coda")
    public boolean isCoda() {
        return "Coda".equals(name);
    }
}
