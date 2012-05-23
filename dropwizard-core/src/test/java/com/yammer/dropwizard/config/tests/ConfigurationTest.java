package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.json.Json;

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

    @Test
    public void ensureConfigSerializable() throws Exception {
        Json mapper = new Json();
        // Issue-96: some types were not serializable
        String json = mapper.writeValueAsString(configuration);
        assertThat(json, is(notNullValue()));

        // and as an added bonus, let's see we can also read it back:
        Configuration cfg = mapper.readValue(json, Configuration.class);
        assertThat(cfg, is(notNullValue()));
    }
}
