package io.dropwizard.auth.basic;

import com.google.common.collect.Lists;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
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

import java.util.List;

public class BasicAuthenticationBundleTest {
    private Authenticator<BasicCredentials, UserIdentity> authenticatorMock;
    private Environment environmentMock;
    private JerseyEnvironment jerseyEnvironmentMock;
    private ResourceConfig resourceConfigMock;
    private boolean requireAuthorization;
    private String realm;
    private List resourceFilters;
    private List containerRequestFilters;


    @Before
    public void setup() {
        authenticatorMock = Mockito.mock(Authenticator.class);
        environmentMock = Mockito.mock(Environment.class);
        jerseyEnvironmentMock = Mockito.mock(JerseyEnvironment.class);
        resourceConfigMock = Mockito.mock(ResourceConfig.class);
        requireAuthorization = true;
        realm = "REALM";
        resourceFilters = Lists.newArrayList();
        containerRequestFilters = Lists.newArrayList();
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
        Mockito.when(resourceConfigMock.getResourceFilterFactories()).thenReturn(resourceFilters);
        Mockito.when(resourceConfigMock.getContainerRequestFilters()).thenReturn(containerRequestFilters);

        BasicAuthenticationBundle basicAuthenticationBundle = new BasicAuthenticationBundle(authenticatorMock, requireAuthorization, realm);
        basicAuthenticationBundle.run(environmentMock);

        Assert.assertEquals(RolesAllowedResourceFilterFactory.class.getName(), resourceFilters.get(0));
        Assert.assertEquals(BasicAuthenticationFilter.class, containerRequestFilters.get(0).getClass());

        Mockito.verify(environmentMock).jersey();
        Mockito.verify(jerseyEnvironmentMock).getResourceConfig();
        Mockito.verify(resourceConfigMock).getResourceFilterFactories();
        Mockito.verify(resourceConfigMock).getContainerRequestFilters();
    }
}
