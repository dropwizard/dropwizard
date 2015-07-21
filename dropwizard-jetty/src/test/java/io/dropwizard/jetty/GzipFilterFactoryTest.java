package io.dropwizard.jetty;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Size;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import static org.assertj.core.api.Assertions.assertThat;

public class GzipFilterFactoryTest {
    private GzipFilterFactory gzip;

    @Before
    public void setUp() throws Exception {
        this.gzip = new ConfigurationFactory<>(GzipFilterFactory.class,
                Validation.buildDefaultValidatorFactory()
                        .getValidator(),
                Jackson.newObjectMapper(), "dw")
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
    public void hasExcludedUserAgents() throws Exception {
        assertThat(gzip.getExcludedUserAgents())
                .isEqualTo(ImmutableSet.of("IE"));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(ImmutableSet.of("text/plain"));
    }

    @Test
    public void varyIsOnlyForAcceptEncoding() throws Exception {
        assertThat(gzip.getVary())
                .isEqualTo("Accept-Encoding");
    }

    @Test
    public void testBuild() {
        final BiDiGzipFilter filter = gzip.build();

        assertThat(filter.getMinGzipSize()).isEqualTo((int) gzip.getMinimumEntitySize().toBytes());
        assertThat(filter.getBufferSize()).isEqualTo((int) gzip.getBufferSize().toBytes());
        assertThat(filter.getExcludedAgents()).containsOnly("IE");
        assertThat(filter.getExcludedAgentPatterns()).hasSize(1);
        assertThat(filter.getExcludedAgentPatterns().iterator().next().pattern()).isEqualTo("OLD-2.+");
        assertThat(filter.getMimeTypes()).containsOnly("text/plain");
        assertThat(filter.getMethods()).containsOnly("GET", "POST");
        assertThat(filter.getDeflateCompressionLevel()).isEqualTo(Deflater.DEFAULT_COMPRESSION);
        assertThat(filter.isInflateNoWrap()).isTrue();
        assertThat(filter.isDeflateNoWrap()).isTrue();
        assertThat(filter.getVary()).isEqualTo("Accept-Encoding");
    }
}
