package com.yammer.dropwizard.config.tests;

import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.RequestLogConfiguration;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RequestLogConfigurationTest {
    private RequestLogConfiguration requestLog;

    @Before
    public void setUp() throws Exception {
        this.requestLog = ConfigurationFactory
                .forClass(RequestLogConfiguration.class, new Validator())
                .build(new File(Resources.getResource("yaml/requestLog.yml").getFile()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(requestLog.getTimeZone(), is(TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void fileConfigurationCanBeEnabled() throws Exception {
        assertThat(requestLog.getFileConfiguration().isEnabled(), is(true));
    }

}
