package com.example.helloworld.core;

import org.junit.Test;

import static com.codahale.dropwizard.testing.JsonHelpers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PersonTest {
    private final Person person = new Person("Luther Blissett", "Football Player");

    @Test
    public void serializesToJSON() throws Exception {
        assertThat("a Person can be serialized to JSON",
                asJson(person),
                is(equalTo(jsonFixture("fixtures/person.json"))));
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertThat("a Person can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/person.json"), Person.class),
                is(person));
    }
}
