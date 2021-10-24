package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JarLocationTest {
    @Test
    void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class))
                .hasToString("project.jar");
    }

    @Test
    void hasAVersion() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).getVersion())
                .isNotPresent();
    }
}
