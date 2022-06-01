package io.dropwizard.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParanamerModuleTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mapper.registerModule(new ParameterNamesModule());
    }

    @Test
    void deserializePersonWithoutAnnotations() throws IOException {
        final ObjectReader reader = mapper.readerFor(Person.class);
        final Person person = reader.readValue("{ \"name\": \"Foo\", \"surname\": \"Bar\" }");
        assertThat(person.getName()).isEqualTo("Foo");
        assertThat(person.getSurname()).isEqualTo("Bar");
    }

    @Test
    void serializePersonWithoutAnnotations() throws IOException {
        final ObjectWriter reader = mapper.writerFor(Person.class);
        final String person = reader.writeValueAsString(new Person("Foo", "Bar"));
        assertThat(person).contains("\"name\":\"Foo\"").contains("\"surname\":\"Bar\"");
    }
}
