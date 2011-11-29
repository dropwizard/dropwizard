package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {
    private final Configuration configuration = new Configuration();

    @Test
    public void hasAnHttpConfiguration() throws Exception {
        assertThat(configuration.getHttpConfiguration(),
                   is(notNullValue()));
    }

    @Test
    public void hasALoggingConfiguration() throws Exception {
        assertThat(configuration.getLoggingConfiguration(),
                   is(notNullValue()));
    }

    @Test
    public void loadsFromTheExampleConfiguration() throws Exception {
        final ConfigurationFactory<Configuration> factory = new ConfigurationFactory<Configuration>(
                Configuration.class,
                new Validator());

        final Configuration config = factory.build(new File("dropwizard/src/test/resources/basic-configuration.yml"));

        assertThat(config.getHttpConfiguration(),
                   is(notNullValue()));
    }
}
