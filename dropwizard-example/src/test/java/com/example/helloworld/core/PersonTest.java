package com.example.helloworld.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

/*
 * This test is used as an example in the docs - if you update it, consider
 * updating the docs too.
 */
class PersonTest {
    private static final ObjectMapper MAPPER = newObjectMapper();

    @Test
    void serializesToJSON() throws Exception {
        final Person person = new Person("Luther Blissett", "Lead Tester", 1902);

        final String expected = MAPPER.writeValueAsString(
            MAPPER.readValue(getClass().getResource("/person.json"), Person.class));

        assertThat(MAPPER.writeValueAsString(person)).isEqualTo(expected);
    }
}
