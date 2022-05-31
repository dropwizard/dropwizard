package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.checkerframework.checker.nullness.qual.Nullable;

public class WrappedFailingExample {
    @Valid
    @Nullable
    private FailingExample example;

    @JsonProperty
    @Nullable
    public FailingExample getExample() {
        return example;
    }

    @JsonProperty
    public void setExample(FailingExample example) {
        this.example = example;
    }
}
