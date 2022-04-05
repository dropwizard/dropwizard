package io.dropwizard.jersey.setup;

import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JerseyEnvironmentTest {

    private final JerseyContainerHolder holder = mock(JerseyContainerHolder.class);
    private final DropwizardResourceConfig config = new DropwizardResourceConfig();
    private final JerseyEnvironment jerseyEnvironment = new JerseyEnvironment(holder, config);

    @Test
    void urlPatternEndsWithSlashStar() {
        assertPatternEndsWithSlashStar("/missing/slash/star");
    }

    @Test
    void urlPatternEndsWithStar() {
        assertPatternEndsWithSlashStar("/missing/star/");
    }

    @Test
    void urlPatternSuffixNoop() {
        String slashStarPath = "/slash/star/*";
        jerseyEnvironment.setUrlPattern(slashStarPath);
        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(slashStarPath);
    }

    private void assertPatternEndsWithSlashStar(String jerseyRootPath) {
        jerseyEnvironment.setUrlPattern(jerseyRootPath);
        assertThat(jerseyEnvironment.getUrlPattern()).endsWith("/*");
    }
}
