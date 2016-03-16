package io.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ConfigurationFactoryTest {

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
        private String name;

        @JsonProperty
        private int age = 1;

        List<String> type;

        @JsonProperty
        private Map<String, String> properties = new LinkedHashMap<>();

        @JsonProperty
        private List<ExampleServer> servers = new ArrayList<>();

        private boolean admin;

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
    }

    static class ExampleWithDefaults {

        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?")
        @JsonProperty
        String name = "Coda Hale";

        @JsonProperty
        List<String> type = ImmutableList.of("coder", "wizard");

        @JsonProperty
        Map<String, String> properties = ImmutableMap.of("debug", "true", "settings.enabled", "false");

        @JsonProperty
        List<ExampleServer> servers = ImmutableList.of(
                ExampleServer.create(8080), ExampleServer.create(8081), ExampleServer.create(8082));
    }

    static class NonInsatiableExample {

        @JsonProperty
        String name = "Code Hale";

        NonInsatiableExample(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    private final Validator validator = BaseValidator.newValidator();
    private final ConfigurationFactory<Example> factory =
            new ConfigurationFactory<>(Example.class, validator, Jackson.newObjectMapper(), "dw");
    private File malformedFile;
    private File emptyFile;
    private File invalidFile;
    private File validFile;

    private static File resourceFileName(String resourceName) throws URISyntaxException {
        return new File(Resources.getResource(resourceName).toURI());
    }

    @After
    public void resetConfigOverrides() {
        for (Enumeration<?> props = System.getProperties().propertyNames(); props.hasMoreElements();) {
            String keyString = (String) props.nextElement();
            if (keyString.startsWith("dw.")) {
                System.clearProperty(keyString);
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        this.malformedFile = resourceFileName("factory-test-malformed.yml");
        this.emptyFile = resourceFileName("factory-test-empty.yml");
        this.invalidFile = resourceFileName("factory-test-invalid.yml");
        this.validFile = resourceFileName("factory-test-valid.yml");
    }

    @Test
    public void loadsValidConfigFiles() throws Exception {
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
    public void handlesSimpleOverride() throws Exception {
        System.setProperty("dw.name", "Coda Hale Overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getName())
            .isEqualTo("Coda Hale Overridden");
    }

    @Test
    public void handlesArrayOverride() throws Exception {
        System.setProperty("dw.type", "coder,wizard,overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(2))
                .isEqualTo("overridden");
        assertThat(example.getType().size())
                .isEqualTo(3);
    }

    @Test
    public void handlesArrayOverrideEscaped() throws Exception {
        System.setProperty("dw.type", "coder,wizard,overr\\,idden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(2))
                .isEqualTo("overr,idden");
        assertThat(example.getType().size())
                .isEqualTo(3);
    }

    @Test
    public void handlesSingleElementArrayOverride() throws Exception {
        System.setProperty("dw.type", "overridden");
        final Example example = factory.build(validFile);
        assertThat(example.getType().get(0))
                .isEqualTo("overridden");
        assertThat(example.getType().size())
                .isEqualTo(1);
    }

    @Test
    public void overridesArrayWithIndices() throws Exception {
        System.setProperty("dw.type[1]", "overridden");
        final Example example = factory.build(validFile);

        assertThat(example.getType().get(0))
                .isEqualTo("coder");
        assertThat(example.getType().get(1))
                .isEqualTo("overridden");
    }

    @Test
    public void overridesArrayWithIndicesReverse() throws Exception {
        System.setProperty("dw.type[0]", "overridden");
        final Example example = factory.build(validFile);

        assertThat(example.getType().get(0))
                .isEqualTo("overridden");
        assertThat(example.getType().get(1))
                .isEqualTo("wizard");
    }

    @Test
    public void overridesArrayPropertiesWithIndices() throws Exception {
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
    public void overrideMapProperty() throws Exception {
        System.setProperty("dw.properties.settings.enabled", "true");
        final Example example = factory.build(validFile);
        assertThat(example.getProperties())
                .contains(MapEntry.entry("debug", "true"),
                        MapEntry.entry("settings.enabled", "true"));
    }

    @Test
    public void throwsAnExceptionOnUnexpectedArrayOverride() throws Exception {
        System.setProperty("dw.servers.port", "9000");
        try {
            factory.build(validFile);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce("target is an array but no index specified");
        }
    }

    @Test(expected = ConfigurationParsingException.class)
    public void throwsAnExceptionOnArrayOverrideWithInvalidType() throws Exception {
        System.setProperty("dw.servers", "one,two");

        factory.build(validFile);
        failBecauseExceptionWasNotThrown(ConfigurationParsingException.class);
    }

    @Test
    public void throwsAnExceptionOnOverrideArrayIndexOutOfBounds() throws Exception {
        System.setProperty("dw.type[2]", "invalid");
        try {
            factory.build(validFile);
            failBecauseExceptionWasNotThrown(ArrayIndexOutOfBoundsException.class);
        } catch (ArrayIndexOutOfBoundsException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce("index is greater than size of array");
        }
    }

    @Test
    public void throwsAnExceptionOnOverrideArrayPropertyIndexOutOfBounds() throws Exception {
        System.setProperty("dw.servers[4].port", "9000");
        try {
            factory.build(validFile);
            failBecauseExceptionWasNotThrown(ArrayIndexOutOfBoundsException.class);
        } catch (ArrayIndexOutOfBoundsException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce("index is greater than size of array");
        }
    }

    @Test
    public void throwsAnExceptionOnMalformedFiles() throws Exception {
        try {
            factory.build(malformedFile);
            failBecauseExceptionWasNotThrown(ConfigurationParsingException.class);
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce(" * Failed to parse configuration; Can not instantiate");
        }
    }

    @Test
    public void throwsAnExceptionOnEmptyFiles() throws Exception {
        try {
            factory.build(emptyFile);
            failBecauseExceptionWasNotThrown(ConfigurationParsingException.class);
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage())
                    .containsOnlyOnce(" * Configuration at " + emptyFile.toString() + " must not be empty");
        }
    }

    @Test
    public void throwsAnExceptionOnInvalidFiles() throws Exception {
        try {
            factory.build(invalidFile);
            failBecauseExceptionWasNotThrown(ConfigurationValidationException.class);
        } catch (ConfigurationValidationException e) {
            if ("en".equals(Locale.getDefault().getLanguage())) {
                assertThat(e.getMessage())
                        .endsWith(String.format(
                                "factory-test-invalid.yml has an error:%n" +
                                        "  * name must match \"[\\w]+[\\s]+[\\w]+([\\s][\\w]+)?\"%n"));
            }
        }
    }

    @Test
    public void handleOverrideDefaultConfiguration() throws Exception {
        System.setProperty("dw.name", "Coda Hale Overridden");
        System.setProperty("dw.type", "coder,wizard,overridden");
        System.setProperty("dw.properties.settings.enabled", "true");
        System.setProperty("dw.servers[0].port", "8090");
        System.setProperty("dw.servers[2].port", "8092");

        final ExampleWithDefaults example =
                new ConfigurationFactory<>(ExampleWithDefaults.class, validator, Jackson.newObjectMapper(), "dw")
                        .build();

        assertThat(example.name).isEqualTo("Coda Hale Overridden");
        assertThat(example.type.get(2)).isEqualTo("overridden");
        assertThat(example.type.size()).isEqualTo(3);
        assertThat(example.properties).containsEntry("settings.enabled", "true");
        assertThat(example.servers.get(0).getPort()).isEqualTo(8090);
        assertThat(example.servers.get(2).getPort()).isEqualTo(8092);
    }

    @Test
    public void handleDefaultConfigurationWithoutOverriding() throws Exception {
        final ExampleWithDefaults example =
                new ConfigurationFactory<>(ExampleWithDefaults.class, validator, Jackson.newObjectMapper(), "dw")
                        .build();

        assertThat(example.name).isEqualTo("Coda Hale");
        assertThat(example.type).isEqualTo(ImmutableList.of("coder", "wizard"));
        assertThat(example.properties).isEqualTo(ImmutableMap.of("debug", "true", "settings.enabled", "false"));
        assertThat(example.servers.get(0).getPort()).isEqualTo(8080);
        assertThat(example.servers.get(1).getPort()).isEqualTo(8081);
        assertThat(example.servers.get(2).getPort()).isEqualTo(8082);
    }

    @Test
    public void throwsAnExceptionIfDefaultConfigurationCantBeInstantiated() throws Exception {
        System.setProperty("dw.name", "Coda Hale Overridden");
        try {
            new ConfigurationFactory<>(NonInsatiableExample.class, validator, Jackson.newObjectMapper(), "dw").build();
            Assert.fail("Configuration is parsed, but shouldn't be");
        } catch (IllegalArgumentException e){
            assertThat(e).hasMessage("Unable create an instance of the configuration class: " +
                    "'io.dropwizard.configuration.ConfigurationFactoryTest.NonInsatiableExample'");
        }

    }

    @Test
    public void printsDidYouMeanOnUnrecognizedField() throws Exception {
        final File resourceFileName = resourceFileName("factory-test-typo.yml");
        try {
            factory.build(resourceFileName);
            fail("Typo in a configuration should be caught");
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage()).isEqualTo(resourceFileName + " has an error:" + NEWLINE +
                    "  * Unrecognized field at: propertis" + NEWLINE +
                    "    Did you mean?:" + NEWLINE +
                    "      - properties" + NEWLINE +
                    "      - servers" + NEWLINE +
                    "      - type" + NEWLINE +
                    "      - name" + NEWLINE +
                    "      - age" + NEWLINE +
                    "        [1 more]" + NEWLINE);
        }
    }

    @Test
    public void incorrectTypeIsFound() throws Exception {
        final File resourceFileName = resourceFileName("factory-test-wrong-type.yml");
        try {
            factory.build(resourceFileName);
            fail("Incorrect type in a configuration should be found");
        } catch (ConfigurationParsingException e) {
            assertThat(e.getMessage()).isEqualTo(resourceFileName + " has an error:" + NEWLINE +
                    "  * Incorrect type of value at: age; is of type: String, expected: int" + NEWLINE);
        }
    }

    @Test
    public void printsDetailedInformationOnMalformedYaml() throws Exception {
        final File resourceFileName = resourceFileName("factory-test-malformed-advanced.yml");
        try {
            factory.build(resourceFileName);
            fail("Should print a detailed error on a malformed YAML file");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo(
                    "YAML decoding problem: while parsing a flow sequence\n" +
                    " in 'reader', line 2, column 7:\n" +
                    "    type: [ coder,wizard\n" +
                    "          ^\n" +
                    "expected ',' or ']', but got StreamEnd\n" +
                    " in 'reader', line 2, column 21:\n" +
                    "    wizard\n" +
                    "          ^" + NEWLINE);
        }
    }
}
