package com.yammer.dropwizard.views.flashscope;

import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class FlashScopeTest extends ResourceTest {

    @Override
    protected void setUpResources() throws Exception {
        addResource(new FlashScopeTestResource());
        addProvider(new FlashScopeInjectableProvider());
        addProvider(new FlashScopeResourceMethodDispatchAdapter());
    }

    @Test
    public void setsFlashScopeContents() throws Exception {
        ClientResponse response = client()
                .resource("/flash-test")
                .post(ClientResponse.class);

        assertThat(response.getCookies(), TestUtils.hasCookieWithName(FlashScope.COOKIE_NAME));
        String decodedValue = URLDecoder.decode(TestUtils.flashCookieIn(response).getValue(), "utf-8");
        assertThat(decodedValue, containsString("It worked"));
    }

    @Test
    public void doesNotSetFlashCookieIfFlashOutIsEmpty() {
        ClientResponse response = client()
                .resource("/flash-empty")
                .post(ClientResponse.class);

        assertThat(response.getCookies(), not(TestUtils.hasCookieWithName(FlashScope.COOKIE_NAME)));
    }

    @Test
    public void retrievesFlashScopeContents() throws Exception {
        String message = client()
                .resource("/flash-return")
                .cookie(new NewCookie(FlashScope.COOKIE_NAME,
                        URLEncoder.encode("{\"actionMessage\":\"Flash aaahhh-ahhhhh\"}", "utf-8")))
                .get(String.class);

        assertThat(message, is("Flash aaahhh-ahhhhh"));
    }

    @Test
    public void canReadNestedValues() throws Exception {
        String message = client()
                .resource("/flash-nested-value")
                .cookie(new NewCookie(FlashScope.COOKIE_NAME,
                        URLEncoder.encode(
                                "{                                  \n" +
                                "  \"outer\": {                     \n" +
                                "    \"inner\": \"Inner value\"     \n" +
                                "  }                                \n" +
                                "}",
                                "utf-8")))
                .get(String.class);

        assertThat(message, is("Inner value"));
    }

    @Test
    public void immediatelyExpiresPreviousFlashCookie() throws Exception {
        ClientResponse response = client()
                .resource("/flash-return")
                .cookie(new NewCookie(FlashScope.COOKIE_NAME,
                    URLEncoder.encode("{\"actionMessage\":\"Should not see this\"}", "utf-8")))
                .get(ClientResponse.class);

        assertThat(response.getCookies(), TestUtils.hasCookieWithName(FlashScope.COOKIE_NAME));
        assertThat(TestUtils.flashCookieIn(response).getMaxAge(), is(0));
    }

    @Path("/")
    public static class FlashScopeTestResource {

        @Path("/flash-test")
        @POST
        public Response doSomething(@FlashScope FlashOut flash) {
            flash.put("actionMessage", "It worked");
            return Response.ok().build();
        }

        @Path("/flash-empty")
        @POST
        public Response doSomethingWithNoFlashOutput(@FlashScope FlashOut flash) {
            return Response.ok().build();
        }

        @Path("/flash-return")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope FlashIn flashIn) {
            return flashIn.get("actionMessage");
        }

        @Path("/flash-nested-value")
        @GET
        @Produces("text/plain")
        public String getNestedValue(@FlashScope FlashIn flashIn) {
            Map<String, String> outer = flashIn.get("outer");
            return outer.get("inner");
        }

    }

}
