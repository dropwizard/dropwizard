package com.yammer.dropwizard.views.flashscope;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;

import static com.yammer.dropwizard.views.flashscope.TestUtils.findCookie;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FlashScopeIntegratedTest {

    @ClassRule
    public static DropwizardServiceRule<TestConfig> RULE =
            new DropwizardServiceRule<TestConfig>(FlashScopeTestService.class,
                                                     resourceFilePath("flash.yml"));

    Client client;

    @Before
    public void init() {
        client = new JerseyClientBuilder()
                .using(RULE.getConfiguration().getJerseyClient())
                .using(newSingleThreadExecutor(), new ObjectMapper())
                .build();
    }

    @Test
    public void endToEndTest() {
        Form form = new Form();
        form.add("message", "Show this in flash");

        String returnedMessage = client.resource(fullUrl("/action")).post(String.class, form);

        assertThat(returnedMessage, is("Show this in flash"));
    }

    @Test
    public void cookieDomainOverridden() {
        ClientResponse response = client.resource(fullUrl("/action-no-redirect")).post(ClientResponse.class);

        assertThat(findCookie(response, FlashScope.COOKIE_NAME).getMaxAge(), is(20));
    }

    private String fullUrl(String relativeUrl) {
        return "http://localhost:" + RULE.getLocalPort() + relativeUrl;
    }

    public static class FlashScopeTestService extends Service<TestConfig> {

        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new FlashScopeBundle<TestConfig>() {
                @Override
                protected FlashScopeConfig getFlashScopeConfig(TestConfig configuration) {
                    return configuration.getFlashScope();
                }
            });
        }

        @Override
        public void run(TestConfig configuration, Environment environment) throws Exception {
            environment.getJerseyEnvironment().addResource(new PostRedirectGetResource());
        }
    }

    @Path("/")
    public static class PostRedirectGetResource {

        @Path("action")
        @POST
        public Response doSomething(@FlashScope FlashOut flash, @FormParam("message") String message) {
            flash.put("message", message);
            return Response.seeOther(URI.create("/result")).build();
        }

        @Path("result")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope FlashIn flash) {
            return flash.get("message");
        }

        @Path("action-no-redirect")
        @POST
        public Response putSomethingInTheFlash(@FlashScope FlashOut flash) {
            flash.put("something", "anything");
            return Response.ok().build();
        }

    }

    public static class TestConfig extends Configuration {

        @JsonProperty
        private FlashScopeConfig flashScope;

        @JsonProperty
        private JerseyClientConfiguration jerseyClient;

        public FlashScopeConfig getFlashScope() {
            return flashScope;
        }

        public JerseyClientConfiguration getJerseyClient() {
            return jerseyClient;
        }
    }


    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
