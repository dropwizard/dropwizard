package com.codahale.dropwizard.config.tests;

import com.google.common.io.Resources;
import com.codahale.dropwizard.config.ConfigurationFactory;
import com.codahale.dropwizard.config.RequestLogConfiguration;
import com.codahale.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

public class RequestLogConfigurationTest {
    private RequestLogConfiguration requestLog;

    @Before
    public void setUp() throws Exception {
        this.requestLog = ConfigurationFactory.forClass(RequestLogConfiguration.class, new Validator())
                                              .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(requestLog.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("UTC"));
    }
}
