package com.codahale.dropwizard.testing.junit;

import com.sun.jersey.api.client.Client;
import org.junit.ClassRule;
import org.junit.Test;

import static com.codahale.dropwizard.testing.junit.ConfigOverride.config;
import static com.codahale.dropwizard.testing.junit.DropwizardAppRuleTest.resourceFilePath;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DropwizardServiceRuleConfigOverrideTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(TestApplication.class,
                                                     resourceFilePath("test-config.yaml"),
                                                     config("message", "A new way to say Hooray!"));

    @Test
    public void supportsConfigAttributeOverrides() {
        final String content = new Client().resource("http://localhost:" + RULE.getLocalPort() + "/test")
                                           .get(String.class);

        assertThat(content, is("A new way to say Hooray!"));
    }
}
