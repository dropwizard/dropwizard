package com.codahale.dropwizard.util.tests;

import com.codahale.dropwizard.util.JarLocation;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class JarLocationTest {
    @Test
    public void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).toString())
                .isEqualTo("project.jar");
    }
}
