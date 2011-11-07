package com.yammer.dropwizard.config.tests;

import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.ConfigurationFactory;
import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationFactoryTest {
    public static class Example {
        @NotNull
        @Pattern(regexp = "[\\w]+[\\s]+[\\w]+")
        @SuppressWarnings("UnusedDeclaration")
        private String name;

        public String getName() {
            return name;
        }
    }

    final ConfigurationFactory<Example> factory = new ConfigurationFactory<Example>(Example.class);
    final File malformedFile = new File(Resources.getResource("factory-test-malformed.yml").getFile());
    final File invalidFile = new File(Resources.getResource("factory-test-invalid.yml").getFile());
    final File validFile = new File(Resources.getResource("factory-test-valid.yml").getFile());

    @Test
    public void loadsValidConfigFiles() throws Exception {
        final Example example = factory.build(validFile);
        assertThat(example.getName(),
                   is("Coda Hale"));
    }

    @Test
    public void throwsAnExceptionOnMalformedFiles() throws Exception {
        try {
            factory.build(malformedFile);
            fail("expected a YAMLException to be thrown, but none was");
        } catch (YAMLException e) {
            assertTrue(true);
        }
    }

    @Test
    public void throwsAnExceptionOnInvalidFiles() throws Exception {
        try {
            factory.build(invalidFile);
        } catch (ConfigurationException e) {
            assertThat(e.getMessage(),
                       endsWith("factory-test-invalid.yml has the following errors:\n" +
                                "  * name must match \"[\\w]+[\\s]+[\\w]+\""));
        }
    }
}
