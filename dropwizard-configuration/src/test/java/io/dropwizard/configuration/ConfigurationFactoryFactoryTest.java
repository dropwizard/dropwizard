package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.BaseConfigurationFactoryTest.Example;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

class ConfigurationFactoryFactoryTest {

    private final ConfigurationSourceProvider configurationSourceProvider = new ResourceConfigurationSourceProvider();
    private final ConfigurationFactoryFactory<Example> factoryFactory = new DefaultConfigurationFactoryFactory<>();
    private final Validator validator = BaseValidator.newValidator();

    @Test
    void createDefaultFactory() throws Exception {
        String validFile = "factory-test-valid.yml";
        ConfigurationFactory<Example> factory =
                factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
        final Example example = factory.build(configurationSourceProvider, validFile);
        assertThat(example.getName()).isEqualTo("Coda Hale");
    }

    @Test
    void createDefaultFactoryFailsUnknownProperty() {
        String validFileWithUnknownProp = "factory-test-unknown-property.yml";
        ConfigurationFactory<Example> factory =
                factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");

        assertThatExceptionOfType(ConfigurationException.class)
                .isThrownBy(() -> factory.build(configurationSourceProvider, validFileWithUnknownProp))
                .withMessageContaining("Unrecognized field at: trait");
    }

    @Test
    void createFactoryAllowingUnknownProperties() throws Exception {
        ConfigurationFactoryFactory<Example> customFactory = new PassThroughConfigurationFactoryFactory();
        String validFileWithUnknownProp = "factory-test-unknown-property.yml";
        ConfigurationFactory<Example> factory = customFactory.create(
                Example.class,
                validator,
                Jackson.newObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "dw");
        Example example = factory.build(configurationSourceProvider, validFileWithUnknownProp);
        assertThat(example.getName()).isEqualTo("Mighty Wizard");
    }

    private static final class PassThroughConfigurationFactoryFactory
            extends DefaultConfigurationFactoryFactory<Example> {
        @Override
        protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
            return objectMapper;
        }
    }
}
