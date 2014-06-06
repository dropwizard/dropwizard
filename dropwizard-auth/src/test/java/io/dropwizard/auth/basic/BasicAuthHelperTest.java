package io.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpRequestContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class BasicAuthHelperTest {

    private HttpRequestContext httpRequestContextMock;

    @Before
    public void setup() {
        httpRequestContextMock = Mockito.mock(HttpRequestContext.class);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(httpRequestContextMock);
    }

    @Test
    public void noAuthHeader_shouldReturnOptionalAbsent() {
        final Optional<BasicCredentials> basicCredentialsOptional = BasicAuthHelper.getBasicCredentialsFromHeader(httpRequestContextMock);

        Assert.assertFalse(basicCredentialsOptional.isPresent());

        Mockito.verify(httpRequestContextMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
    }

    @Test
    public void withBrokenAuthHeader_shouldReturnBasicCredentials() {
        Mockito.when(httpRequestContextMock.getHeaderValue(HttpHeaders.AUTHORIZATION)).thenReturn("dXNlcm5hbWU6cGFzc3dvcmQ=");

        final Optional<BasicCredentials> basicCredentialsOptional = BasicAuthHelper.getBasicCredentialsFromHeader(httpRequestContextMock);

        Assert.assertFalse(basicCredentialsOptional.isPresent());

        Mockito.verify(httpRequestContextMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
    }

    @Test
    public void withAuthHeader_shouldReturnBasicCredentials() {
        Mockito.when(httpRequestContextMock.getHeaderValue(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");

        final Optional<BasicCredentials> basicCredentialsOptional = BasicAuthHelper.getBasicCredentialsFromHeader(httpRequestContextMock);

        Assert.assertTrue(basicCredentialsOptional.isPresent());

        final BasicCredentials basicCredentials = basicCredentialsOptional.get();
        Assert.assertEquals("username", basicCredentials.getUsername());
        Assert.assertEquals("password", basicCredentials.getPassword());

        Mockito.verify(httpRequestContextMock).getHeaderValue(HttpHeaders.AUTHORIZATION);
    }

    @Test
    public void createUnauthorizedResponse() {
        final Response response = BasicAuthHelper.createUnauthorizedResponse("REALM");

        Assert.assertEquals(401, response.getStatus());
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMetadata().getFirst("Content-Type"));
        Assert.assertEquals("Basic realm=\"REALM\"", response.getMetadata().getFirst("WWW-Authenticate"));
        Assert.assertEquals("Credentials are required to access this resource.", response.getEntity());
    }
}
