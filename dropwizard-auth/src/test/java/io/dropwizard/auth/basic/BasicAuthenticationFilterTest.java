package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.sun.jersey.spi.container.ContainerRequest;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;

public class BasicAuthenticationFilterTest {
    private Authenticator<BasicCredentials, UserIdentity> authenticatorMock;
    private UserIdentity userIdentityMock;
    private ContainerRequest containerRequestMock;
    private boolean requireAuthorization;
    private String realm;
    private BasicAuthenticationFilter basicAuthenticationFilter;

    @Before
    public void setup() {
        authenticatorMock = Mockito.mock(Authenticator.class);
        userIdentityMock = Mockito.mock(UserIdentity.class);
        containerRequestMock = Mockito.mock(ContainerRequest.class);

        requireAuthorization = true;
        realm = "REALM";
        basicAuthenticationFilter = new BasicAuthenticationFilter(authenticatorMock, requireAuthorization, realm);
    }

    @After
    public void after() {
        Mockito.verifyNoMoreInteractions(containerRequestMock);
        Mockito.verifyNoMoreInteractions(authenticatorMock);
        Mockito.verifyNoMoreInteractions(userIdentityMock);
    }

    @Test(expected = WebApplicationException.class)
    public void noAuthHeaderRequireAuth_filter_shouldThrowWebApplicationException() {
        try {
            basicAuthenticationFilter.filter(containerRequestMock);
        } finally {
            Mockito.verify(containerRequestMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
        }
    }

    @Test
    public void noAuthHeader_filter_shouldThrowWebApplicationException() {
        requireAuthorization = false;
        basicAuthenticationFilter = new BasicAuthenticationFilter(authenticatorMock, requireAuthorization, realm);
        final ContainerRequest filter = basicAuthenticationFilter.filter(containerRequestMock);

        Assert.assertEquals(containerRequestMock, filter);

        Mockito.verify(containerRequestMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
    }

    @Test
    public void withAuthHeader_filter_shouldThrowWebApplicationException() throws AuthenticationException {
        Mockito.when(authenticatorMock.authenticate(Mockito.any(BasicCredentials.class))).thenReturn(Optional.of(userIdentityMock));
        Mockito.when(containerRequestMock.getHeaderValue(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
        final ContainerRequest filter = basicAuthenticationFilter.filter(containerRequestMock);

        Assert.assertEquals(containerRequestMock, filter);

        Mockito.verify(authenticatorMock).authenticate(Mockito.any(BasicCredentials.class));
        Mockito.verify(containerRequestMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
        Mockito.verify(containerRequestMock).setSecurityContext(Mockito.any(SecurityContext.class));
    }

    @Test
    public void authenticate() throws AuthenticationException {
        Mockito.when(authenticatorMock.authenticate(Mockito.any(BasicCredentials.class))).thenReturn(Optional.of(userIdentityMock));

        final Optional<UserIdentity> authenticate = basicAuthenticationFilter.authenticate(Optional.of(new BasicCredentials("username", "password")));

        Assert.assertTrue(authenticate.isPresent());

        Mockito.verify(authenticatorMock).authenticate(Mockito.any(BasicCredentials.class));
    }
}
