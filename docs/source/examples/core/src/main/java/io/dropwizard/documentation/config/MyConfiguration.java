package io.dropwizard.documentation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class MyConfiguration extends Configuration {
    @Valid
    @NotNull
    private String myParam;

    @JsonProperty("myParam")
    public String getMyParam() {
        return myParam;
    }

    @JsonProperty("myParam")
    public void setMyParam(String myParam) {
        this.myParam = myParam;
    }
}
