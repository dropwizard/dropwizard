package com.yammer.dropwizard.testing;

import com.yammer.dropwizard.json.Json;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

import static com.yammer.dropwizard.testing.FixtureHelpers.fixture;

/**
 * A set of helper methods for testing the serialization and deserialization of classes to and from
 * JSON.
 * <p>For example, a test for reading and writing a {@code Person} object as JSON:</p>
 * <pre><code>
 * assertThat("writing a person as JSON produces the appropriate JSON object",
 *            asJson(person),
 *            is(jsonFixture("fixtures/person.json"));
 *
 * assertThat("reading a JSON object as a person produces the appropriate person",
 *            fromJson(jsonFixture("fixtures/person.json"), Person.class),
 *            is(person));
 * </code></pre>
 */
public class JsonHelpers {
    private static final Json JSON = new Json();

    private JsonHelpers() { /* singleton */ }

    /**
     * Converts the given object into a JSON AST.
     *
     * @param object    an object
     * @return {@code object} as a JSON AST node
     * @throws IOException if there is an error writing {@code object} as JSON
     */
    public static JsonNode asJson(Object object) throws IOException {
        return JSON.writeValueAsTree(object);
    }

    /**
     * Converts the given JSON AST into an object of the given type.
     *
     * @param json     a JSON AST
     * @param klass    the class of the type that {@code json} should be converted to
     * @param <T>      the type that {@code json} should be converted to
     * @return {@code json} as an instance of {@code T}
     * @throws IOException if there is an error reading {@code json} as an instance of {@code T}
     */
    public static <T> T fromJson(JsonNode json, Class<T> klass) throws IOException {
        return JSON.readValue(json, klass);
    }

    /**
     * Converts the given JSON AST into an object of the given type.
     *
     * @param json         a JSON AST
     * @param reference    a reference of the type that {@code json} should be converted to
     * @param <T>          the type that {@code json} should be converted to
     * @return {@code json} as an instance of {@code T}
     * @throws IOException if there is an error reading {@code json} as an instance of {@code T}
     */
    public static <T> T fromJson(JsonNode json, TypeReference<T> reference) throws IOException {
        return JSON.readValue(json, reference);
    }

    public static JsonNode jsonFixture(String filename) throws IOException {
        return JSON.readValue(fixture(filename), JsonNode.class);
    }
}
