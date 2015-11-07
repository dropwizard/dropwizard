package io.dropwizard.jetty;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Size;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.zip.Deflater;

import static org.assertj.core.api.Assertions.assertThat;

public class GzipHandlerFactoryTest {
    private GzipHandlerFactory gzip;

    @Before
    public void setUp() throws Exception {
        this.gzip = new ConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
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
                .isEqualTo(Size.kilobytes(12));
    }

    @Test
    public void hasABufferSize() throws Exception {
        assertThat(gzip.getBufferSize())
                .isEqualTo(Size.kilobytes(32));
    }

    @Test
    public void hasExcludedUserAgentPatterns() throws Exception {
        assertThat(gzip.getExcludedUserAgentPatterns())
                .isEqualTo(ImmutableSet.of("OLD-2.+"));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(ImmutableSet.of("text/plain"));
    }

    @Test
    public void testBuild() {
        final BiDiGzipHandler handler = gzip.build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo((int) gzip.getMinimumEntitySize().toBytes());
        assertThat(handler.getExcludedAgentPatterns()).hasSize(1);
        assertThat(handler.getExcludedAgentPatterns()[0]).isEqualTo("OLD-2.+");
        assertThat(handler.getIncludedMimeTypes()).containsOnly("text/plain");
        assertThat(handler.getIncludedMethods()).containsOnly("GET", "POST");
        assertThat(handler.getCompressionLevel()).isEqualTo(Deflater.DEFAULT_COMPRESSION);
        assertThat(handler.isInflateNoWrap()).isTrue();
    }

    @Test
    public void testBuildDefault() throws Exception {
        final BiDiGzipHandler handler = new ConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new File(Resources.getResource("yaml/default_gzip.yml").toURI()))
                .build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo(256);
        assertThat(handler.getExcludedAgentPatterns()).isEmpty();
        assertThat(handler.getIncludedMimeTypes()).isEmpty(); // All apart excluded
        assertThat(handler.getIncludedMethods()).containsOnly("GET");
        assertThat(handler.getCompressionLevel()).isEqualTo(Deflater.DEFAULT_COMPRESSION);
        assertThat(handler.isInflateNoWrap()).isTrue();
    }
}
