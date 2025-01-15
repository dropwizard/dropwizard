package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.datatype.guava.deser.util.RangeHelper;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AnnotationSensitivePropertyNamingStrategyTest {
    public static class RegularExample {
        @JsonProperty
        @Nullable
        String firstName;

        @SuppressWarnings({ "UnusedDeclaration", "unused" }) // Jackson
        private RegularExample() {}

        public RegularExample(String firstName) {
            this.firstName = firstName;
        }
    }

    @JsonSnakeCase
    public static class SnakeCaseExample {
        @JsonProperty
        @Nullable
        String firstName;

        @SuppressWarnings({ "UnusedDeclaration", "unused" }) // Jackson
        private SnakeCaseExample() {}

        public SnakeCaseExample(String firstName) {
            this.firstName = firstName;
        }
    }

    private final PropertyNamingStrategy strategy = new AnnotationSensitivePropertyNamingStrategy();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper.setPropertyNamingStrategy(strategy);
    }

    @Test
    void serializesRegularProperties() throws Exception {
        assertThat(mapper.writeValueAsString(new RegularExample("woo")))
                .isEqualTo("{\"firstName\":\"woo\"}");
    }

    @Test
    void serializesSnakeCaseProperties() throws Exception {
        assertThat(mapper.writeValueAsString(new SnakeCaseExample("woo")))
                .isEqualTo("{\"first_name\":\"woo\"}");
    }

    @Test
    void deserializesRegularProperties() throws Exception {
        assertThat(mapper.readValue("{\"firstName\":\"woo\"}", RegularExample.class).firstName)
                .isEqualTo("woo");
    }

    @Test
    void deserializesSnakeCaseProperties() throws Exception {
        assertThat(mapper.readValue("{\"first_name\":\"woo\"}", SnakeCaseExample.class).firstName)
                .isEqualTo("woo");
    }

    @Test
    void nameForConstructorParameterWorksWithNullField() {
        final MapperConfig<?> mapperConfig = mock(MapperConfig.class);
        final String name = strategy.nameForConstructorParameter(mapperConfig, null, "defaultName");
        assertThat(name).isEqualTo("defaultName");
    }

    @Test
    void nameForFieldWorksWithNullField() {
        final MapperConfig<?> mapperConfig = mock(MapperConfig.class);
        final String name = strategy.nameForField(mapperConfig, null, "defaultName");
        assertThat(name).isEqualTo("defaultName");
    }

    @Test
    void nameForGetterMethodWorksWithNullField() {
        final MapperConfig<?> mapperConfig = mock(MapperConfig.class);
        final String name = strategy.nameForGetterMethod(mapperConfig, null, "defaultName");
        assertThat(name).isEqualTo("defaultName");
    }

    @Test
    void nameForSetterMethodWorksWithNullField() {
        final MapperConfig<?> mapperConfig = mock(MapperConfig.class);
        final String name = strategy.nameForSetterMethod(mapperConfig, null, "defaultName");
        assertThat(name).isEqualTo("defaultName");
    }

    @Test
    // https://github.com/dropwizard/dropwizard/issues/3514
    void usingRangeHelperDoesNotThrowNullPointerException() {
        final RangeHelper.RangeProperties standardNames = RangeHelper.standardNames();
        assertThat(standardNames.lowerBoundType).isNotBlank();
        assertThat(standardNames.lowerEndpoint).isNotBlank();
        assertThat(standardNames.upperBoundType).isNotBlank();
        assertThat(standardNames.upperEndpoint).isNotBlank();
    }
}
