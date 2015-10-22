package io.dropwizard.client.proxy;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationParsingException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class HttpClientConfigurationTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private HttpClientConfiguration configuration;

    private void load(String configLocation) throws Exception {
        configuration = new ConfigurationFactory<>(HttpClientConfiguration.class,
                Validators.newValidator(),
                objectMapper, "dw")
                .build(new File(Resources.getResource(configLocation).toURI()));
    }

    @Test
    public void testNoProxy() throws Exception {
        load("./yaml/no_proxy.yml");
        assertThat(configuration.getProxyConfiguration()).isNull();
    }

    @Test
    public void testFullConfig() throws Exception {
        load("yaml/proxy.yml");

        ProxyConfiguration proxy = configuration.getProxyConfiguration();
        assertThat(proxy).isNotNull();

        assertThat(proxy.getHost()).isEqualTo("192.168.52.11");
        assertThat(proxy.getPort()).isEqualTo(8080);
        assertThat(proxy.getScheme()).isEqualTo("https");

        AuthConfiguration auth = proxy.getAuth();
        assertThat(auth).isNotNull();
        assertThat(auth.getUsername()).isEqualTo("secret");
        assertThat(auth.getPassword()).isEqualTo("stuff");

        List<String> nonProxyHosts = proxy.getNonProxyHosts();
        assertThat(nonProxyHosts).contains("localhost", "192.168.52.*", "*.example.com");
    }

    @Test
    public void testNoScheme() throws Exception {
        load("./yaml/no_scheme.yml");

        ProxyConfiguration proxy = configuration.getProxyConfiguration();
        assertThat(proxy).isNotNull();
        assertThat(proxy.getHost()).isEqualTo("192.168.52.11");
        assertThat(proxy.getPort()).isEqualTo(8080);
        assertThat(proxy.getScheme()).isEqualTo("http");
    }

    @Test
    public void testNoAuth() throws Exception {
        load("./yaml/no_auth.yml");

        ProxyConfiguration proxy = configuration.getProxyConfiguration();
        assertThat(proxy).isNotNull();
        assertThat(proxy.getHost()).isNotNull();
        assertThat(proxy.getAuth()).isNull();
    }

    @Test
    public void testNoPort() throws Exception {
        load("./yaml/no_port.yml");

        ProxyConfiguration proxy = configuration.getProxyConfiguration();
        assertThat(proxy).isNotNull();
        assertThat(proxy.getHost()).isNotNull();
        assertThat(proxy.getPort()).isEqualTo(-1);
    }

    @Test
    public void testNoNonProxy() throws Exception {
        load("./yaml/no_port.yml");

        ProxyConfiguration proxy = configuration.getProxyConfiguration();
        assertThat(proxy.getNonProxyHosts()).isNull();
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testNoHost() throws Exception {
        load("yaml/bad_host.yml");
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testBadPort() throws Exception {
        load("./yaml/bad_port.yml");
    }

    @Test(expected = ConfigurationParsingException.class)
    public void testBadScheme() throws Exception {
        load("./yaml/bad_scheme.yml");
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testBadAuthUsername() throws Exception {
        load("./yaml/bad_auth_username.yml");
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testBadPassword() throws Exception {
        load("./yaml/bad_auth_password.yml");
    }

}
