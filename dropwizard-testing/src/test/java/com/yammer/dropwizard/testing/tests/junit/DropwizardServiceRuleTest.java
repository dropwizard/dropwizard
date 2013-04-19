package com.yammer.dropwizard.testing.tests.junit;

import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DropwizardServiceRuleTest {

    @ClassRule
    public static final DropwizardServiceRule<TestConfiguration> RULE =
            new DropwizardServiceRule<TestConfiguration>(TestService.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = new Client().resource("http://localhost:" +
                RULE.getLocalPort()
                + "/test").get(String.class);

        assertThat(content, is("Yes, it's here"));
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = RULE.getConfiguration();
        assertThat(config.getMessage(), is("Yes, it's here"));
        assertThat(config.getHttpConfiguration().getPort(), is(0));
    }

    @Test
    public void returnsService() {
        final TestService service = RULE.getService();
        assertNotNull(service);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = RULE.getEnvironment();
        assertThat(environment.getName(), is("TestService"));
    }

    public static String resourceFilePath(String resourceClassPathLocation) {
        try {

            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
