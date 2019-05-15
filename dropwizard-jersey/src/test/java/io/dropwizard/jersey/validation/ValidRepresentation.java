package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;

public class ValidRepresentation {
    @NotEmpty
    private String name = "";

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }
}
