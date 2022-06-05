package io.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.DefaultObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ConfigurationMetadataTest {
    private final ObjectMapper objectMapper = new DefaultObjectMapperFactory().newObjectMapper();

    @SuppressWarnings("UnusedDeclaration")
    public static class ExampleConfiguration {

        @JsonProperty
        private int port = 8000;

        @JsonProperty
        private ExampleInterface example = new DefaultExampleInterface();

        @JsonProperty
        private ExampleInterfaceWithDefaultImpl exampleWithDefault = new DefaultExampleInterface();

        @JsonProperty
        private List<ExampleInterfaceWithDefaultImpl> exampleWithDefaults = new ArrayList<>();

        public int getPort() {
            return port;
        }

        public ExampleInterface getExample() {
            return example;
        }

        public ExampleInterfaceWithDefaultImpl getExampleWithDefault() {
            return exampleWithDefault;
        }

        public List<ExampleInterfaceWithDefaultImpl> getExampleWithDefaults() {
            return exampleWithDefaults;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public interface ExampleInterface {

    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultExampleInterface.class)
    public interface ExampleInterfaceWithDefaultImpl {

    }

    @SuppressWarnings("UnusedDeclaration")
    public static class DefaultExampleInterface implements ExampleInterface,
            ExampleInterfaceWithDefaultImpl {

        @JsonProperty
        private String[] array = new String[]{};

        @JsonProperty
        private List<String> list = Collections.emptyList();

        @JsonProperty
        private Set<String> set = Collections.emptySet();

        public String[] getArray() {
            return array;
        }

        public List<String> getList() {
            return list;
        }

        public Set<String> getSet() {
            return set;
        }
    }

    public static class Issue3528Configuration {
        @JsonProperty
        public ObjectMapper getMapper() {
            return new ObjectMapper();
        }
    }

    public static class SelfReferencingConfiguration {
        private String str = "test";

        @JsonProperty
        public SelfReferencingConfiguration getSelfReferencingConfiguration() {
            return new SelfReferencingConfiguration();
        }

        @JsonProperty
        public String getStr() {
            return str;
        }
    }

    public static class SelfReferencingIgnoredConfiguration {
        private String str = "test";
        private Long number = 42L;
        @JsonIgnore
        private SelfReferencingConfiguration ignored = new SelfReferencingConfiguration();

        @JsonIgnore
        public SelfReferencingConfiguration getSelfReferencingConfiguration() {
            return new SelfReferencingConfiguration();
        }

        @JsonProperty
        public String getStr() {
            return str;
        }

        public Long getNumber() {
            return number;
        }

        public SelfReferencingConfiguration getIgnored() {
            return ignored;
        }
    }

    @ParameterizedTest
    @MethodSource("provideArgsForShouldDiscoverAllFields")
    void shouldDiscoverAllFields(String name, boolean isPrimitive,
                                        boolean isCollectionOrArrayType,
                                        Class<?> klass) {
        final ConfigurationMetadata metadata = new ConfigurationMetadata(
                objectMapper, ExampleConfiguration.class);

        assertThat(metadata.fields.get(name)).isNotNull().satisfies((f) -> {
            assertThat(f.isPrimitive()).isEqualTo(isPrimitive);
            assertThat(f.isCollectionLikeType() || f.isArrayType())
                    .isEqualTo(isCollectionOrArrayType);

            if (isCollectionOrArrayType) {
                assertThat(f.getContentType().isTypeOrSubTypeOf(klass)).isTrue();
            } else {
                assertThat(f.isTypeOrSubTypeOf(klass)).isTrue();
            }
        });
    }

    private static Stream<Arguments> provideArgsForShouldDiscoverAllFields() {
        return Stream.of(
                Arguments.of("port", true, false, Integer.TYPE),
                Arguments.of("example", false, false, ExampleInterface.class),
                Arguments.of("exampleWithDefault.array", false, true, String.class),
                Arguments.of("exampleWithDefault.list", false, true, String.class),
                Arguments.of("exampleWithDefault.set", false, true, String.class),
                Arguments.of("exampleWithDefaults[*].array", false, true, String.class),
                Arguments.of("exampleWithDefaults[*].list", false, true, String.class),
                Arguments.of("exampleWithDefaults[*].set", false, true, String.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsForIsCollectionOfStringsShouldWork")
    void isCollectionOfStringsShouldWork(String name, boolean isCollectionOfStrings) {
        final ConfigurationMetadata metadata = new ConfigurationMetadata(
                objectMapper, ExampleConfiguration.class);

        assertThat(metadata.isCollectionOfStrings(name)).isEqualTo(isCollectionOfStrings);
    }

    private static Stream<Arguments> provideArgsForIsCollectionOfStringsShouldWork() {
        return Stream.of(
                Arguments.of("doesnotexist", false),
                Arguments.of("port", false),
                Arguments.of("example.array", false),
                Arguments.of("example.list", false),
                Arguments.of("example.set", false),
                Arguments.of("exampleWithDefault.array", true),
                Arguments.of("exampleWithDefault.list", true),
                Arguments.of("exampleWithDefault.set", true),
                Arguments.of("exampleWithDefaults[0].array", true),
                Arguments.of("exampleWithDefaults[0].list", true),
                Arguments.of("exampleWithDefaults[0].set", true)
        );
    }

    @Test
    void issue3528ShouldNotProduceOutOfMemoryError() {
        assertThatNoException().isThrownBy(
                () -> new ConfigurationMetadata(objectMapper, Issue3528Configuration.class));
    }

    @Test
    void fieldsAnnotatedWithJsonIgnoreShouldBeIgnored() {
        final ConfigurationMetadata metadata =
                new ConfigurationMetadata(objectMapper, SelfReferencingIgnoredConfiguration.class);

        assertThat(metadata.fields).containsOnlyKeys("str", "number");
    }

    @Test
    void selfReferencingConfigurationShouldNotLoop() {
        final ConfigurationMetadata metadata =
                new ConfigurationMetadata(objectMapper, SelfReferencingConfiguration.class);

        assertThat(metadata.fields).containsOnlyKeys("selfReferencingConfiguration.str", "str");
    }
}
