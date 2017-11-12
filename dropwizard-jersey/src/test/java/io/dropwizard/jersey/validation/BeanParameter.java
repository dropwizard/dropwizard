package io.dropwizard.jersey.validation;

import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.Objects;

public class BeanParameter {
    @QueryParam("name")
    @NotEmpty
    private String name = "";

    public String getName() {
        return name;
    }

    @QueryParam("choice")
    @NotNull
    @Nullable
    private Choice choice;

    public Choice getChoice() {
        return Objects.requireNonNull(choice);
    }

    @ValidationMethod(message = "name must be Coda")
    public boolean isCoda() {
        return "Coda".equals(name);
    }
}
