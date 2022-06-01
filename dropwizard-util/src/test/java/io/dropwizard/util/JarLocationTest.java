package io.dropwizard.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JarLocationTest {
    @Test
    void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class)).hasToString("project.jar");
    }

    @Test
    void hasAVersion() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).getVersion()).isNotPresent();
    }
}
