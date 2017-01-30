package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.BaseConfigurationFactoryTest.Example;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.Validator;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigurationFactoryFactoryTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final ConfigurationFactoryFactory<Example> factoryFactory = new DefaultConfigurationFactoryFactory<>();
    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void createDefaultFactory() throws Exception {
        File validFile = new File(Resources.getResource("factory-test-valid.yml").toURI());
        ConfigurationFactory<Example> factory =
            factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
        final Example example = factory.build(validFile);
        assertThat(example.getName())
            .isEqualTo("Coda Hale");
    }

    @Test
    public void createDefaultFactoryFailsUnknownProperty() throws Exception {
        File validFileWithUnknownProp = new File(
            Resources.getResource("factory-test-unknown-property.yml").toURI());
        ConfigurationFactory<Example> factory =
            factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Unrecognized field at: trait");
        factory.build(validFileWithUnknownProp);
    }

    @Test
    public void createFactoryAllowingUnknownProperties() throws Exception {
        ConfigurationFactoryFactory<Example> customFactory = new PassThroughConfigurationFactoryFactory();
        File validFileWithUnknownProp = new File(
            Resources.getResource("factory-test-unknown-property.yml").toURI());
        ConfigurationFactory<Example> factory =
            customFactory.create(
                Example.class,
                validator,
                Jackson.newObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "dw");
        Example example = factory.build(validFileWithUnknownProp);
        assertThat(example.getName())
            .isEqualTo("Mighty Wizard");
    }

    private static final class PassThroughConfigurationFactoryFactory
            extends DefaultConfigurationFactoryFactory<Example> {
        @Override
        protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
            return objectMapper;
        }
    }
}
