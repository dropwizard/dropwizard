package com.yammer.dropwizard.config.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.ConfigurationException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfigurationExceptionTest {
    @Test
    public void formatsTheViolationsIntoAHumanReadableMessage() throws Exception {
        final ConfigurationException e = new ConfigurationException("config.yml",
                                                                    ImmutableList.of("woo may not be null"));

        assertThat(e.getMessage())
                .isEqualTo("config.yml has the following errors:\n" +
                                   "  * woo may not be null\n");
    }
}
