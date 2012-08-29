package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.JarLocation;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class JarLocationTest {
    @Test
    public void isHumanReadable() throws Exception {
        assertThat(new JarLocation(JarLocationTest.class).toString())
                .isEqualTo("project.jar");
    }
}
