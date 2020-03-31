package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.selfvalidating.SelfValidating;
import io.dropwizard.validation.selfvalidating.SelfValidation;
import io.dropwizard.validation.selfvalidating.ViolationCollector;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

@SelfValidating
public class SelfValidatingClass {

    @Nullable
    @JsonProperty
    private Integer answer;

    public SelfValidatingClass() { }

    public SelfValidatingClass(@NotNull @QueryParam("answer") Integer answer) {
        this.answer = answer;
    }


    @SelfValidation
    public void validate(ViolationCollector collector) {
        if (answer == null || !answer.equals(42)) {
            collector.addViolation("The answer is 42");
        }
    }

}
