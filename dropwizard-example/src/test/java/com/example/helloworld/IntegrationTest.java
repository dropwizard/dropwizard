package com.example.helloworld;

import com.example.helloworld.core.Saying;
import com.google.common.base.Optional;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static org.assertj.core.api.Assertions.assertThat;


public class IntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<HelloWorldConfiguration> RULE = new DropwizardAppRule<>(
            HelloWorldApplication.class, ResourceHelpers.resourceFilePath("test-example.yml"));

    @Test
    public void testHelloWorld() throws Exception {
        final Optional<String> name = Optional.fromNullable("Dr. IntegrationTest");
        final Client client = ClientBuilder.newClient();
        final Saying saying = client.target("http://localhost:" + RULE.getLocalPort() + "/hello-world")
                .queryParam("name", name.get())
                .request()
                .get(Saying.class);
        assertThat(saying.getContent()).isEqualTo(RULE.getConfiguration().buildTemplate().render(name));
        client.close();
    }
}
