package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.JarLocation;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JarLocationTest {
    @Test
    public void isHumanReadable() throws Exception {
        assertThat(new JarLocation().toString(),
                   is("project.jar"));
    }
}
