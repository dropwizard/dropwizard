package io.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Maps;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class BaseConfigurationFactoryTest {

    private static final String NEWLINE = System.lineSeparator();

    @SuppressWarnings("UnusedDeclaration")
    public static class ExampleServer {

        @JsonProperty
        private int port = 8000;

        public int getPort() {
            return port;
        }

        public static ExampleServer create(int port) {
            ExampleServer server = new ExampleServer();
            server.port = port;
            return server;
        }

    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Example {

        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?")
        private String name = "";

        @JsonProperty
        private int age = 1;

        List<String> type = Collections.emptyList();

        @JsonProperty
        private Map<String, String> properties = Collections.emptyMap();

        @JsonProperty
        private List<ExampleServer> servers = Collections.emptyList();

        private boolean admin;

        @JsonProperty("my.logger")
        private Map<String, String> logger = Collections.emptyMap();

        public String getName() {
            return name;
        }

        public List<String> getType() {
            return type;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public List<ExampleServer> getServers() {
            return servers;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public Map<String, String> getLogger() {
            return logger;
        }
    }

    static class ExampleWithDefaults {

        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?")
        @JsonProperty
        String name = "Coda Hale";

        @JsonProperty
        List<String> type = Arrays.asList("coder", "wizard");

        @JsonProperty
        Map<String, String> properties = Maps.of("debug", "true", "settings.enabled", "false");

        @JsonProperty
        List<ExampleServer> servers = Arrays.asList(
                ExampleServer.create(8080), ExampleServer.create(8081), ExampleServer.create(8082));

        @JsonProperty
        @Valid
        CaffeineSpec cacheBuilderSpec = CaffeineSpec.parse("initialCapacity=0,maximumSize=0");
    }

    static class NonInsatiableExample {

        @JsonProperty
        String name = "Code Hale";

        NonInsatiableExample(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    protected final Validator validator = BaseValidator.newValidator();
    protected ConfigurationFactory<Example> factory = new ConfigurationFactory<Example>() {
        @Override
        public Example build(ConfigurationSourceProvider provider, String path) {
            return new Example();
        }

        @Override
        public Example build() {
            return new Example();
        }
    };
    protected File malformedFile = new File("/");
    protected File emptyFile = new File("/");
    protected File invalidFile = new File("/");
    protected File validFile = new File("/");
    protected File validNoTypeFile = new File("/");
    protected File typoFile = new File("/");
    protected File wrongTypeFile = new File("/");
    protected File malformedAdvancedFile = new File("/");

    protected static File resourceFileName(String resourceName) throws URISyntaxException {
        return new File(Resources.getResource(resourceName).toURI());
    }

    @AfterEach
    void resetConfigOverrides() {
        for (Enumeration<?> props = System.getProperties().propertyNames(); props.hasMoreElements();) {
            String keyString = (String) props.nextElement();
            if (keyString.startsWith("dw.")) {
                System.clearProperty(keyString);
            }
        }
    }

    @Test
    void usesDefaultedCacheBuilderSpec() throws Exception {
        final ExampleWithDefaults example =
            new YamlConfigurationFactory<>(ExampleWithDefaults.class, validator, Jackson.newObjectMapper(), "dw")
                .build();
        assertThat(example.cacheBuilderSpec)
            .isNotNull()
            .isEqualTo(CaffeineSpec.parse("initialCapacity=0,maximumSize=0"));
    }

    @Test
    void loadsValidConfigFiles() throws Exception {
        final Example example = factory.build(validFile);

        assertThat(example.getName())
                .isEqualTo("Coda Hale");

        assertThat(example.getType().get(0))
                .isEqualTo("coder");
        assertThat(example.getType().get(1))
                .isEqualTo("wizard");

        assertThat(example.getProperties())
                .contains(MapEntry.entry("debug", "true"),
                        MapEntry.entry("settings.enabled", "false"));

        assertThat(example.getServers())
                .hasSize(3);
        assertThat(example.getServers().get(0).getPort())
                .isEqualTo(8080);

    }

    @Test
    void handlesSimpleOverride() throws Exception {
        System.setProperty("dw.name", "Coda Hale Overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getName())
            .isEqualTo("Coda Hale Overridden");
    }

    @Test
    void handlesExistingOverrideWithPeriod() throws Exception {
        System.setProperty("dw.my\\.logger.level", "debug");
        final Example example = factory.build(validFile);
        assertThat(example.getLogger().get("level"))
            .isEqualTo("debug");
    }

    @Test
    void handlesNewOverrideWithPeriod() throws Exception {
        System.setProperty("dw.my\\.logger.com\\.example", "error");
        final Example example = factory.build(validFile);
        assertThat(example.getLogger().get("com.example"))
            .isEqualTo("error");
    }

    @Test
    void handlesArrayOverride() throws Exception {
        System.setProperty("dw.type", "coder,wizard,overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(2))
                .isEqualTo("overridden");
        assertThat(example.getType().size())
                .isEqualTo(3);
    }

    @Test
    void handlesArrayOverrideEscaped() throws Exception {
        System.setProperty("dw.type", "coder,wizard,overr\\,idden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(2))
                .isEqualTo("overr,idden");
        assertThat(example.getType().size())
                .isEqualTo(3);
    }

    @Test
    void handlesSingleElementArrayOverride() throws Exception {
        System.setProperty("dw.type", "overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(0))
                .isEqualTo("overridden");
        assertThat(example.getType().size())
                .isEqualTo(1);
    }

    @Test
    void handlesArrayOverrideIntoValidNoTypeFile() throws Exception {
        System.setProperty("dw.type", "coder,wizard,overridden");
        final Example example = factory.build(validNoTypeFile);
        assertThat(example.getType().get(2))
            .isEqualTo("overridden");
        assertThat(example.getType().size())
            .isEqualTo(3);
    }

    @Test
    void overridesArrayWithIndices() throws Exception {
        System.setProperty("dw.type[1]", "overridden");
        final Example example = factory.build(validFile);

        assertThat(example.getType().get(0))
                .isEqualTo("coder");
        assertThat(example.getType().get(1))
                .isEqualTo("overridden");
    }

    @Test
    void overridesArrayWithIndicesReverse() throws Exception {
        System.setProperty("dw.type[0]", "overridden");
        final Example example = factory.build(validFile);

        assertThat(example.getType().get(0))
                .isEqualTo("overridden");
        assertThat(example.getType().get(1))
                .isEqualTo("wizard");
    }

    @Test
    void overridesArrayPropertiesWithIndices() throws Exception {
        System.setProperty("dw.servers[0].port", "7000");
        System.setProperty("dw.servers[2].port", "9000");
        final Example example = factory.build(validFile);

        assertThat(example.getServers())
                .hasSize(3);
        assertThat(example.getServers().get(0).getPort())
                .isEqualTo(7000);
        assertThat(example.getServers().get(2).getPort())
                .isEqualTo(9000);
    }

    @Test
    void overrideMapProperty() throws Exception {
        System.setProperty("dw.properties.settings.enabled", "true");
        final Example example = factory.build(validFile);
        assertThat(example.getProperties())
                .contains(MapEntry.entry("debug", "true"),
                        MapEntry.entry("settings.enabled", "true"));
    }

    @Test
    void throwsAnExceptionOnUnexpectedArrayOverride() {
        System.setProperty("dw.servers.port", "9000");
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> factory.build(validFile))
            .withMessageContaining("target is an array but no index specified");
    }

    @Test
    void throwsAnExceptionOnArrayOverrideWithInvalidType() {
        System.setProperty("dw.servers", "one,two");

        assertThatExceptionOfType(ConfigurationParsingException.class).isThrownBy(() -> factory.build(validFile));
    }

    @Test
    void throwsAnExceptionOnOverrideArrayIndexOutOfBounds() {
        System.setProperty("dw.type[2]", "invalid");
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
            .isThrownBy(() -> factory.build(validFile))
            .withMessageContaining("index is greater than size of array");
    }

    @Test
    void throwsAnExceptionOnOverrideArrayPropertyIndexOutOfBounds() {
        System.setProperty("dw.servers[4].port", "9000");
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
            .isThrownBy(() -> factory.build(validFile))
            .withMessageContaining("index is greater than size of array");
    }

    @Test
    void throwsAnExceptionOnMalformedFiles() {
        assertThatExceptionOfType(ConfigurationParsingException.class)
            .isThrownBy(() -> factory.build(malformedFile));
    }

    @Test
    void throwsAnExceptionOnEmptyFiles() {
        assertThatExceptionOfType(ConfigurationParsingException.class)
            .isThrownBy(() -> factory.build(emptyFile))
            .withMessageContaining(" * Configuration at " + emptyFile.toString() + " must not be empty");
    }

    @Test
    void throwsAnExceptionOnInvalidFiles() {
        ThrowableAssertAlternative<ConfigurationValidationException> t = assertThatExceptionOfType(ConfigurationValidationException.class)
            .isThrownBy(() -> factory.build(invalidFile));

        if ("en".equals(Locale.getDefault().getLanguage())) {
            t.withMessageEndingWith(String.format(
                    "%s has an error:%n  * name must match \"[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?\"%n",
                    invalidFile.getName()));
        }
    }

    @Test
    void handleOverrideDefaultConfiguration() throws Exception {
        System.setProperty("dw.name", "Coda Hale Overridden");
        System.setProperty("dw.type", "coder,wizard,overridden");
        System.setProperty("dw.properties.settings.enabled", "true");
        System.setProperty("dw.servers[0].port", "8090");
        System.setProperty("dw.servers[2].port", "8092");

        final ExampleWithDefaults example =
                new YamlConfigurationFactory<>(ExampleWithDefaults.class, validator, Jackson.newObjectMapper(), "dw")
                        .build();

        assertThat(example.name).isEqualTo("Coda Hale Overridden");
        assertThat(example.type.get(2)).isEqualTo("overridden");
        assertThat(example.type.size()).isEqualTo(3);
        assertThat(example.properties).containsEntry("settings.enabled", "true");
        assertThat(example.servers.get(0).getPort()).isEqualTo(8090);
        assertThat(example.servers.get(2).getPort()).isEqualTo(8092);
    }

    @Test
    void handleDefaultConfigurationWithoutOverriding() throws Exception {
        final ExampleWithDefaults example =
                new YamlConfigurationFactory<>(ExampleWithDefaults.class, validator, Jackson.newObjectMapper(), "dw")
                        .build();

        assertThat(example.name).isEqualTo("Coda Hale");
        assertThat(example.type).isEqualTo(Arrays.asList("coder", "wizard"));
        assertThat(example.properties).isEqualTo(Maps.of("debug", "true", "settings.enabled", "false"));
        assertThat(example.servers.get(0).getPort()).isEqualTo(8080);
        assertThat(example.servers.get(1).getPort()).isEqualTo(8081);
        assertThat(example.servers.get(2).getPort()).isEqualTo(8082);
    }

    @Test
    void throwsAnExceptionIfDefaultConfigurationCantBeInstantiated() {
        System.setProperty("dw.name", "Coda Hale Overridden");
        final YamlConfigurationFactory<NonInsatiableExample> factory =
            new YamlConfigurationFactory<>(NonInsatiableExample.class, validator, Jackson.newObjectMapper(), "dw");
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(factory::build)
            .withMessage("Unable to create an instance of the configuration class: " +
                "'io.dropwizard.configuration.BaseConfigurationFactoryTest.NonInsatiableExample'");
    }

    @Test
    void incorrectTypeIsFound() {
        assertThatExceptionOfType(ConfigurationParsingException.class)
            .isThrownBy(() -> factory.build(wrongTypeFile))
            .withMessage(String.format("%s has an error:" + NEWLINE +
                "  * Incorrect type of value at: age; is of type: String, expected: int" + NEWLINE, wrongTypeFile));
    }

    @Test
    void printsDetailedInformationOnMalformedContent() throws Exception {
        factory.build(malformedAdvancedFile);
    }
}
