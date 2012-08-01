package com.yammer.dropwizard.client;

import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpClientConfigurationTest {

    private HttpClientConfiguration httpClientConfig;

    @Test
    public void acceptsValidDnsOverrides() throws Exception {
        load("http-client-valid.yaml");

        assertThat(httpClientConfig.getDnsOverrides().lookUp("google.com"), is(InetAddress.getByName("1.2.3.4")));
        assertThat(httpClientConfig.getDnsOverrides().lookUp("yahoo.com"), is(InetAddress.getByName("2.3.4.5")));
    }

    @Test(expected = JsonMappingException.class)
    public void rejectsDnsOverridesWithValuesThatAreNotValidInetAddresses() throws Exception {
        load("http-client-invalid.yaml");
    }

    private void load(String yamlFile) throws Exception {
        httpClientConfig = ConfigurationFactory.forClass(HttpClientConfiguration.class,
                new Validator())
                .build(new File(Resources.getResource("yaml/" + yamlFile).getFile()));
    }
}
