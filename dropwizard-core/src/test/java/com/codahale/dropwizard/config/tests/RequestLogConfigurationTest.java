package com.codahale.dropwizard.config.tests;

import com.codahale.dropwizard.config.ConfigurationFactory;
import com.codahale.dropwizard.config.RequestLogConfiguration;
import com.codahale.dropwizard.jackson.Jackson;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

public class RequestLogConfigurationTest {
    private RequestLogConfiguration requestLog;

    @Before
    public void setUp() throws Exception {
        this.requestLog = new ConfigurationFactory<>(RequestLogConfiguration.class,
                                                     Validation.buildDefaultValidatorFactory()
                                                               .getValidator(),
                                                     Jackson.newObjectMapper())
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(requestLog.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("UTC"));
    }
}
