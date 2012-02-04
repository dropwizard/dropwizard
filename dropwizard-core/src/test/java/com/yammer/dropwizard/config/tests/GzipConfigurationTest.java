package com.yammer.dropwizard.config.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.GzipConfiguration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class GzipConfigurationTest {
    private GzipConfiguration gzip;

    @Before
    public void setUp() throws Exception {
        this.gzip = ConfigurationFactory.forClass(GzipConfiguration.class,
                                                  new Validator()).build(new File(Resources.getResource("yaml/gzip.yml").getFile()));
    }

    @Test
    public void canBeEnabled() throws Exception {
        assertThat(gzip.isEnabled(),
                   is(false));
    }

    @Test
    public void hasAMinimumEntitySize() throws Exception {
        assertThat(gzip.getMinimumEntitySize(),
                   is(Optional.of(Size.kilobytes(12))));
    }

    @Test
    public void hasABufferSize() throws Exception {
        assertThat(gzip.getBufferSize(),
                   is(Optional.of(Size.kilobytes(32))));
    }

    @Test
    public void hasExcludedUserAgents() throws Exception {
        assertThat(gzip.getExcludedUserAgents(),
                   is(Optional.of(ImmutableSet.of("IE"))));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes(),
                   is(Optional.of(ImmutableSet.of("text/plain"))));
    }
}
