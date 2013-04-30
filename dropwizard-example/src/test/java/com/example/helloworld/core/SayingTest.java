package com.example.helloworld.core;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SayingTest {
    @Test
    public void isValid() throws Exception {
        ImmutableList<String> errors = new Validator().validate(fromJson(jsonFixture("fixtures/saying.json"), Saying.class));
        assertThat(errors.size(),
                is(0));
    }

    @Test
    public void isInvalid() throws Exception {
        ImmutableList<String> errors = new Validator().validate(fromJson(jsonFixture("fixtures/saying_invalid.json"), Saying.class));
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
