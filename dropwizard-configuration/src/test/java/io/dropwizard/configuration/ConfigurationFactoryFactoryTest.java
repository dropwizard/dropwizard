package io.dropwizard.configuration;

import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactoryTest.Example;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validator;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigurationFactoryFactoryTest {

    private final ConfigurationFactoryFactory<Example> factoryFactory = new DefaultConfigurationFactoryFactory<>();
    private final Validator validator = BaseValidator.newValidator();
    private File validFile;

    @Before
    public void setUp() throws Exception {
        this.validFile = new File(Resources.getResource("factory-test-valid.yml").toURI());
    }

     @Test
     public void createDefaultFactory() throws Exception {
         ConfigurationFactory<Example> factory = factoryFactory.create(Example.class, validator, Jackson.newObjectMapper(), "dw");
         final Example example = factory.build(validFile);
         assertThat(example.getName())
                 .isEqualTo("Coda Hale");
     }
}
