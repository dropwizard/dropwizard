package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.BaseConfigurationFactoryTest.Example;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;

import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class ConfigurationFactoryFactoryTest {

    private final ConfigurationFactoryFactory<Example> factoryFactory = new DefaultConfigurationFactoryFactory<>();
    private final Validator validator = BaseValidator.newValidator();

    @Test
    void createDefaultFactory() throws Exception {
        File validFile = new File(Resources.getResource("factory-test-valid.yml").toURI());
        ConfigurationFactory<Example> factory =
            factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
        final Example example = factory.build(validFile);
        assertThat(example.getName())
            .isEqualTo("Coda Hale");
    }

    @Test
    void createDefaultFactoryAllowsUnknownProperty() throws Exception {
        File validFileWithUnknownProp = new File(
            Resources.getResource("factory-test-unknown-property.yml").toURI());
        ConfigurationFactory<Example> factory =
            factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
        Example example = factory.build(validFileWithUnknownProp);

        assertThat(example.getName())
            .isEqualTo("Mighty Wizard");
    }

    @Test
    void createFactoryNotAllowingUnknownProperties() throws Exception {
        ConfigurationFactoryFactory<Example> customFactory = new UnknownPropertiesNotAllowedConfigurationFactoryFactory();
        File validFileWithUnknownProp = new File(
            Resources.getResource("factory-test-unknown-property.yml").toURI());
        ConfigurationFactory<Example> factory =
            customFactory.create(
                Example.class,
                validator,
                Jackson.newObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "dw");

        assertThatExceptionOfType(ConfigurationException.class)
            .isThrownBy(() -> factory.build(validFileWithUnknownProp))
            .withMessageContaining("Unrecognized field at: trait");
    }

    private static final class UnknownPropertiesNotAllowedConfigurationFactoryFactory
        extends DefaultConfigurationFactoryFactory<Example> {

        @Override
        protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
            return objectMapper;
        }
    }
}
