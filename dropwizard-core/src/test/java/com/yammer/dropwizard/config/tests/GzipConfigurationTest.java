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

import static org.fest.assertions.api.Assertions.assertThat;

public class GzipConfigurationTest {
    private GzipConfiguration gzip;

    @Before
    public void setUp() throws Exception {
        this.gzip = ConfigurationFactory.forClass(GzipConfiguration.class,
                                                  new Validator())
                                        .build(new File(Resources.getResource("yaml/gzip.yml").toURI()));
    }

    @Test
    public void canBeEnabled() throws Exception {
        assertThat(gzip.isEnabled())
                .isFalse();
    }

    @Test
    public void hasAMinimumEntitySize() throws Exception {
        assertThat(gzip.getMinimumEntitySize())
                .isEqualTo(Optional.of(Size.kilobytes(12)));
    }

    @Test
    public void hasABufferSize() throws Exception {
        assertThat(gzip.getBufferSize())
                .isEqualTo(Optional.of(Size.kilobytes(32)));
    }

    @Test
    public void hasExcludedUserAgents() throws Exception {
        assertThat(gzip.getExcludedUserAgents())
                .isEqualTo(Optional.of(ImmutableSet.of("IE")));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(Optional.of(ImmutableSet.of("text/plain")));
    }
}
