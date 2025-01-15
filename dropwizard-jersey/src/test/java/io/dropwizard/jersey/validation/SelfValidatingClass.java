package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.selfvalidating.SelfValidating;
import io.dropwizard.validation.selfvalidating.SelfValidation;
import io.dropwizard.validation.selfvalidating.ViolationCollector;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import org.jspecify.annotations.Nullable;

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
