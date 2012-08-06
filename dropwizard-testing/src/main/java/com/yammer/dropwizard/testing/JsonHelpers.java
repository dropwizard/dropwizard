package com.yammer.dropwizard.testing;

import static com.yammer.dropwizard.testing.FixtureHelpers.fixture;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;

import com.yammer.dropwizard.json.Json;

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
     * Converts the given object into a canonical JSON string.
     *
     * @param object    an object
     * @return {@code object} as a JSON string
     * @throws IllegalArgumentException if there is an error encoding {@code object}
     */
    public static String asJson(Object object) throws IllegalArgumentException {
        return JSON.writeValueAsString(object);
    }

    /**
     * Converts the given JSON string into an object of the given type.
     *
     * @param json     a JSON string
     * @param klass    the class of the type that {@code json} should be converted to
     * @param <T>      the type that {@code json} should be converted to
     * @return {@code json} as an instance of {@code T}
     * @throws IOException if there is an error reading {@code json} as an instance of {@code T}
     */
    public static <T> T fromJson(String json, Class<T> klass) throws IOException {
        return JSON.readValue(json, klass);
    }

    /**
     * Converts the given JSON string into an object of the given type.
     *
     * @param json         a JSON string
     * @param reference    a reference of the type that {@code json} should be converted to
     * @param <T>          the type that {@code json} should be converted to
     * @return {@code json} as an instance of {@code T}
     * @throws IOException if there is an error reading {@code json} as an instance of {@code T}
     */
    public static <T> T fromJson(String json, TypeReference<T> reference) throws IOException {
        return JSON.readValue(json, reference);
    }

    /**
     * Loads the given fixture resource as a normalized JSON string.
     *
     * @param filename    the filename of the fixture
     * @return the contents of {@code filename} as a normalized JSON string
     * @throws IOException if there is an error parsing {@code filename}
     */
    public static String jsonFixture(String filename) throws IOException {
        return JSON.writeValueAsString(JSON.readValue(fixture(filename), JsonNode.class));
    }
    
    /**
     * This method will "round trip" test the supplied {@code type} by first serializing it to 
     * JSON and compare it against the fixture file named as:
     * "{@code instance.getClass().getSimpleName().toLowerCase()}.json" and then
     * deserializing the same fixture file to an instance of {@code T}
     * 
     * @param type       the instance to serialize and deserialize
     * @param <T>        the type that {@code json} should be converted to
     * @return the fixture as an instance of {@code T}
     * @throws Exception if the fixture file can't be deserialized into an instance of {@code T}
     */
    @SuppressWarnings("unchecked")
    public static <T> T jsonRoundTrip(T type) throws Exception {
        final String klass = type.getClass().getName();
        final String fixtureName = type.getClass().getSimpleName().toLowerCase();
        final String fixture = jsonFixture("fixtures/" + fixtureName + ".json");
        assertThat(klass + " bad json,",
                asJson(type),
                is(fixture));
        try {
            return fromJson(fixture, (Class<T>)type.getClass());
        }
        catch(final Exception e) {
            throw new Exception("Can't deserialize:\n" + asJson(type), e);
        }
    }

    /**
     * This method will first call {@link JsonHelpers#jsonRoundTrip(Object)} and in addition
     * also verify that the supplied {@code type} is equal to the deserialized instance and
     * that they both return the same hashcode.
     * 
     * @param type the instance to serialize and deserialize
     * @return the fixture as an instance of {@code T}
     * @throws Exception if the fixture file can't be deserialized into an instance of {@code T}
     */
    public static <T> T jsonRoundTripWithEquality(T type) throws Exception {
        final String klass = type.getClass().getName();
        final T deserialized = jsonRoundTrip(type);
        assertEquals(klass + " bad equals,",
                deserialized,
                type);
        assertEquals(klass + " bad hashcode,",
                deserialized.hashCode(),
                type.hashCode());
        return deserialized;
    }

}
