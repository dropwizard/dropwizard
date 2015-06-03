package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

public class WrappedFailingExample {
    @Valid
    private FailingExample example;

    @JsonProperty
    public FailingExample getExample() {
        return example;
    }

    @JsonProperty
    public void setExample(FailingExample example) {
        this.example = example;
    }
}
