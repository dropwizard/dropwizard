package com.yammer.dropwizard.testing.tests;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonHelpersTest {
    private final String json = "{\"name\":\"Coda\",\"email\":\"coda@example.com\"}";

    @Test
    public void readsJsonFixturesAsJsonNodes() throws Exception {
        assertThat(jsonFixture("fixtures/person.json"),
                   is(json));
    }

    @Test
    public void convertsObjectsIntoJson() throws Exception {
        assertThat(asJson(new Person("Coda", "coda@example.com")),
                   is(jsonFixture("fixtures/person.json")));
    }

    @Test
    public void convertsJsonIntoObjects() throws Exception {
        assertThat(fromJson(jsonFixture("fixtures/person.json"), Person.class),
                   is(new Person("Coda", "coda@example.com")));

        assertThat(fromJson(jsonFixture("fixtures/person.json"), new TypeReference<Person>() {}),
                   is(new Person("Coda", "coda@example.com")));
    }
    
    @Test
    public void succesfulRoundTrip() throws Exception {
        jsonRoundTrip(new Person("Coda", "coda@example.com"));
    }

    @Test(expected = AssertionError.class)
    public void failingRoundTrip() throws Exception {
        jsonRoundTrip(new Person("Mårten", "marten@example.com"));
    }

    @Test
    public void succesfulRoundTripWithEquality() throws Exception {
        jsonRoundTripWithEquality(new Person("Coda", "coda@example.com"));
    }

    @Test(expected = AssertionError.class)
    public void roundTripWithBadEquals() throws Exception {
        jsonRoundTripWithEquality(new BadEquality());
    }
    
    public static class BadEquality {
        @JsonProperty private String value = "value";
        
        BadEquality() { /* Jackson deserialization */ }
    }
}
