package com.yammer.dropwizard.auth.basic;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.User;
import org.eclipse.jetty.util.B64Code;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicAuthInjectableTest {
    private final Authenticator<BasicCredentials, User> authenticator = new Authenticator<BasicCredentials, User>() {
        @Override
        public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
            if ("good".equals(credentials.getPassword())) {
                return Optional.of(new User(credentials.getUsername()));
            }

            if ("bad".equals(credentials.getPassword())) {
                throw new AuthenticationException("OH NOE");
            }

            return Optional.absent();
        }
    };
    private final BasicAuthInjectable<User> required = new BasicAuthInjectable<User>(authenticator,
                                                                                     "Realm",
                                                                                     true);

    private final BasicAuthInjectable<User> optional = new BasicAuthInjectable<User>(authenticator,
                                                                                     "Realm",
                                                                                     false);

    private final HttpContext context = mock(HttpContext.class);
    private final HttpRequestContext requestContext = mock(HttpRequestContext.class);

    @Before
    public void setUp() throws Exception {
        when(context.getRequest()).thenReturn(requestContext);
    }

    @Test
    public void requiredAuthWithMissingHeaderReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn(null);

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithMissingHeaderReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn(null);

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithBadSchemeReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basically WAUGH");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithBadSchemeReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basically WAUGH");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithNoSchemeReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basically_WAUGH");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithNoSchemeReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basically_WAUGH");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithMalformedCredsReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("poops"));

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithMalformedCredsReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("poops"));

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithReallyMalformedCredsReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic woofff");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithReallyMalformedCredsReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic woofff");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithUtf8CredsReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic AK0AnAk=");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void requiredAuthWithBadCredsReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("dude:mop"));

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithBadCredsReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("dude:mop"));

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithGoodCredsReturnsAUser() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("dude:good"));

        assertThat(required.getValue(context),
                   is(new User("dude")));
    }

    @Test
    public void optionalAuthWithGoodCredsReturnsAUser() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("dude:good"));

        assertThat(optional.getValue(context),
                   is(new User("dude")));
    }
    @Test
    public void authenticatorFailureReturnsInternalServerError() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Basic " + B64Code.encode("dude:bad"));

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus(),
                       is(500));
        }
    }

    private void assertUnauthorized(WebApplicationException e) {
        final Response response = e.getResponse();

        assertThat(response.getStatus(),
                   is(401));

        assertThat(response.getMetadata().getFirst("WWW-Authenticate").toString(),
                   is("Basic realm=\"Realm\""));

        assertThat(response.getMetadata().getFirst("Content-Type").toString(),
                   is("text/plain"));

        assertThat(response.getEntity().toString(),
                   is("Credentials are required to access this resource."));
    }
}
