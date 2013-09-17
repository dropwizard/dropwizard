package com.example.helloworld.core;

import com.codahale.dropwizard.validation.ConstraintViolations;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static com.codahale.dropwizard.testing.JsonHelpers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SayingTest {
    @Test
    public void isValid() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ImmutableList<String> errors = ConstraintViolations.format(
                validator.validate(fromJson(jsonFixture("fixtures/saying.json"), Saying.class)));
        assertThat(errors.size(),
                is(0));
    }

    @Test
    public void isInvalid() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ImmutableList<String> errors = ConstraintViolations.format(
                validator.validate(fromJson(jsonFixture("fixtures/saying_invalid.json"), Saying.class)));
        assertThat(errors.get(0),
                is("content length must be between 0 and 3 (was abcdef)"));
    }

    @Test
    public void serializesToJSON() throws Exception {
        assertThat("a Saying can be serialized to JSON",
                asJson(new Saying(101l, "abc")),
                is(equalTo(jsonFixture("fixtures/saying.json"))));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertThat("a Saying can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/saying.json"), Saying.class),
                is(new Saying(101l, "abc")));
    }
}
