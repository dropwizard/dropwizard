package com.yammer.dropwizard.config.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.ConfigurationException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationExceptionTest {
    @Test
    public void formatsTheViolationsIntoAHumanReadableMessage() throws Exception {
        final ConfigurationException e = new ConfigurationException("config.yml",
                                                                    ImmutableList.of("woo may not be null"));

        assertThat(e.getMessage(),
                   is("config.yml has the following errors:\n" +
                      "  * woo may not be null\n"));
    }
}
