package com.codahale.dropwizard.util;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class JarLocationTest {
    @Test
    public void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).toString())
                .isEqualTo("project.jar");
    }

    @Test
    public void hasAVersion() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).getVersion())
                .isEqualTo(Optional.<String>absent());
    }
}
