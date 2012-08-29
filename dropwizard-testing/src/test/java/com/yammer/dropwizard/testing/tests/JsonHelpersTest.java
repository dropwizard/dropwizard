package com.yammer.dropwizard.testing.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class JsonHelpersTest {
    @Test
    public void readsJsonFixturesAsJsonNodes() throws Exception {
        final String json = "{\"name\":\"Coda\",\"email\":\"coda@example.com\"}";
        assertThat(jsonFixture("fixtures/person.json"))
                .isEqualTo(json);
    }

    @Test
    public void convertsObjectsIntoJson() throws Exception {
        assertThat(asJson(new Person("Coda", "coda@example.com")))
                .isEqualTo(jsonFixture("fixtures/person.json"));
    }

    @Test
    public void convertsJsonIntoObjects() throws Exception {
        assertThat(fromJson(jsonFixture("fixtures/person.json"), Person.class))
                .isEqualTo(new Person("Coda", "coda@example.com"));

        assertThat(fromJson(jsonFixture("fixtures/person.json"), new TypeReference<Person>() {}))
                .isEqualTo(new Person("Coda", "coda@example.com"));
    }
}
