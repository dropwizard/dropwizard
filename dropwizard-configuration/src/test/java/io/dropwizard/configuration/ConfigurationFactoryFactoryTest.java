package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.configuration.ConfigurationFactoryTest.Example;
import io.dropwizard.jackson.Jackson;

import java.io.File;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;


public class ConfigurationFactoryFactoryTest {
        
    private final ConfigurationFactoryFactory<Example> factoryFactory = new DefaultConfigurationFactoryFactory<Example>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
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
