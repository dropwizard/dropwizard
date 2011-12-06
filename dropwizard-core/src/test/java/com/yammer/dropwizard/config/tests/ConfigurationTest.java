package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.Configuration;
import org.junit.Test;

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
}
