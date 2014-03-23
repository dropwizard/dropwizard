package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationSensitivePropertyNamingStrategyTest {
    public static class RegularExample {
        @JsonProperty
        String firstName;

        @SuppressWarnings("UnusedDeclaration") // Jackson
        private RegularExample() {}

        public RegularExample(String firstName) {
            this.firstName = firstName;
        }
    }

    @JsonSnakeCase
    public static class SnakeCaseExample {
        @JsonProperty
        String firstName;

        @SuppressWarnings("UnusedDeclaration") // Jackson
        private SnakeCaseExample() {}

        public SnakeCaseExample(String firstName) {
            this.firstName = firstName;
        }
    }

    private final PropertyNamingStrategy strategy = new AnnotationSensitivePropertyNamingStrategy();
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mapper.setPropertyNamingStrategy(strategy);
    }

    @Test
    public void serializesRegularProperties() throws Exception {
        assertThat(mapper.writeValueAsString(new RegularExample("woo")))
                .isEqualTo("{\"firstName\":\"woo\"}");
    }

    @Test
    public void serializesSnakeCaseProperties() throws Exception {
        assertThat(mapper.writeValueAsString(new SnakeCaseExample("woo")))
                .isEqualTo("{\"first_name\":\"woo\"}");
    }

    @Test
    public void deserializesRegularProperties() throws Exception {
        assertThat(mapper.readValue("{\"firstName\":\"woo\"}", RegularExample.class).firstName)
                .isEqualTo("woo");
    }

    @Test
    public void deserializesSnakeCaseProperties() throws Exception {
        assertThat(mapper.readValue("{\"first_name\":\"woo\"}", SnakeCaseExample.class).firstName)
                .isEqualTo("woo");
    }
}
