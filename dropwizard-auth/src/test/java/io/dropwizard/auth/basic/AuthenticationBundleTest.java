package io.dropwizard.auth.basic;

import com.google.common.collect.Maps;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

public class AuthenticationBundleTest {
    private Authenticator<BasicCredentials, UserIdentity> authenticatorMock;
    private Environment environmentMock;
    private JerseyEnvironment jerseyEnvironmentMock;
    private ResourceConfig resourceConfigMock;
    private boolean requireAuthorization;
    private String realm;
    private Map<String, Object> properties;


    @Before
    public void setup() {
        authenticatorMock = Mockito.mock(Authenticator.class);
        environmentMock = Mockito.mock(Environment.class);
        jerseyEnvironmentMock = Mockito.mock(JerseyEnvironment.class);
        resourceConfigMock = Mockito.mock(ResourceConfig.class);
        requireAuthorization = true;
        realm = "REALM";
        properties = Maps.newHashMap();
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(authenticatorMock);
        Mockito.verifyNoMoreInteractions(environmentMock);
        Mockito.verifyNoMoreInteractions(jerseyEnvironmentMock);
        Mockito.verifyNoMoreInteractions(resourceConfigMock);
    }

    @Test
    public void test() {
        Mockito.when(environmentMock.jersey()).thenReturn(jerseyEnvironmentMock);
        Mockito.when(jerseyEnvironmentMock.getResourceConfig()).thenReturn(resourceConfigMock);
        Mockito.when(resourceConfigMock.getProperties()).thenReturn(properties);

        AuthenticationBundle authenticationBundle = new AuthenticationBundle(authenticatorMock, requireAuthorization, realm);
        authenticationBundle.run(environmentMock);

        Assert.assertEquals("com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory", properties.get(AuthenticationBundle.RESOURCE_FILTERS));
        Assert.assertEquals("io.dropwizard.auth.basic.AuthenticationFilter", properties.get(AuthenticationBundle.CONTAINER_REQUEST_FILTERS).toString().split("@")[0]);

        Mockito.verify(environmentMock).jersey();
        Mockito.verify(jerseyEnvironmentMock).getResourceConfig();
        Mockito.verify(resourceConfigMock, Mockito.times(2)).getProperties();
    }
}
