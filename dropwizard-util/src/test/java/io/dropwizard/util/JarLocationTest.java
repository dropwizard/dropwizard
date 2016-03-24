package io.dropwizard.util;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JarLocationTest {
    @Test
    public void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).toString())
                .isEqualTo("project.jar");
    }

    @Test
    public void hasAVersion() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).getVersion())
                .isEqualTo(Optional.empty());
    }
}
