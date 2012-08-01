package com.yammer.dropwizard.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpClientTest {

    private static final int HTTP_STUBS_PORT = 8090;

    private HttpClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(HTTP_STUBS_PORT);

    @Before
    public void init() throws Exception {
        HttpClientConfiguration httpClientConfig = ConfigurationFactory.forClass(HttpClientConfiguration.class,
                new Validator())
                .build(new File(Resources.getResource("yaml/http-client-with-dns-overrides.yaml").getFile()));

        client = new HttpClientFactory(httpClientConfig).build();
    }

    @Test
    public void factoryShouldConstructAnHttpClientWithDnsOverrides() throws Exception {
        stubFor(get(urlEqualTo("/something")).willReturn(aResponse().withBody("Fake google")));
        stubFor(get(urlEqualTo("/another-thing")).willReturn(aResponse().withBody("Made up domain")));

        assertThat(bodyFrom("http://google.com:" + HTTP_STUBS_PORT + "/something"), is("Fake google"));
        assertThat(bodyFrom("http://some.nonexistent.domain.co.ck:" + HTTP_STUBS_PORT + "/another-thing"), is("Made up domain"));

    }

    private String bodyFrom(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);

        return EntityUtils.toString(response.getEntity());
    }
}
