package com.yammer.dropwizard.config.tests;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.GzipConfiguration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class GzipConfigurationTest {
    private GzipConfiguration gzip;

    @Before
    public void setUp() throws Exception {
        File gzipFile = new File(Resources.getResource("yaml/gzip.yml").toURI());
        this.gzip = ConfigurationFactory.forClass(GzipConfiguration.class,
                new Validator())
                                        .build(gzipFile.toString(), new FileInputStream(gzipFile));
    }

    @Test
    public void canBeEnabled() throws Exception {
        assertThat(gzip.isEnabled())
                .isFalse();
    }

    @Test
    public void hasAMinimumEntitySize() throws Exception {
        assertThat(gzip.getMinimumEntitySize())
                .isEqualTo(Size.kilobytes(12));
    }

    @Test
    public void hasABufferSize() throws Exception {
        assertThat(gzip.getBufferSize())
                .isEqualTo(Size.kilobytes(32));
    }

    @Test
    public void hasExcludedUserAgents() throws Exception {
        assertThat(gzip.getExcludedUserAgents())
                .isEqualTo(ImmutableSet.of("IE"));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(ImmutableSet.of("text/plain"));
    }
}
