package com.yammer.dropwizard.testing.tests;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonHelpersTest {
    private final JsonNodeFactory factory = JsonNodeFactory.instance;
    private final ObjectNode json = factory.objectNode();
    {
        json.put("name", "Coda");
        json.put("email", "coda@example.com");
    }

    @Test
    public void readsJsonFixturesAsJsonNodes() throws Exception {
        assertThat(jsonFixture("fixtures/person.json"),
                   is((JsonNode) json));
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
}
