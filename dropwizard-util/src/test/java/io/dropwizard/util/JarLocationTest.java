package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JarLocationTest {
    @Test
    void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).toString())
                .isEqualTo("project.jar");
    }

    @Test
    void hasAVersion() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).getVersion())
                .isEqualTo(Optional.empty());
    }
}
