package io.dropwizard.cdi;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WeldApplicationTest {

    @Rule
    public final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(WeldTestApplication.class);

    @Test
    public void dummyResourceRequest() throws Exception {
        String response = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() + "/dummy").request().get(String.class);
        assertThat(response, is(DummyProducer.DUMMY));
    }
}
