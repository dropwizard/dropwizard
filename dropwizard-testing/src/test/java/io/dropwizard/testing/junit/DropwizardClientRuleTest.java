package io.dropwizard.testing.junit;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DropwizardClientRuleTest {

    @ClassRule
    public static final DropwizardClientRule RULE_WITH_INSTANCE = new DropwizardClientRule(new TestResource("foo"));

    @ClassRule
    public static final DropwizardClientRule RULE_WITH_CLASS = new DropwizardClientRule(TestResource.class);

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = new JerseyClientBuilder()
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 5000)
            .build();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final String response = client.target(RULE_WITH_INSTANCE.baseUri() + "/test").request().get(String.class);
        assertEquals("foo", response);
    }

    @Test
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final String response = client.target(RULE_WITH_CLASS.baseUri() + "/test").request().get(String.class);
        assertEquals(TestResource.DEFAULT_MESSAGE, response);
    }
}
