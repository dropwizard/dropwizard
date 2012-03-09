package com.yammer.dropwizard.auth.oauth;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthInjectableTest {
    private static class User {
        private final String token;

        private User(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if ((o == null) || (getClass() != o.getClass())) { return false; }
            final User user = (User) o;
            return token.equals(user.token);
        }

        @Override
        public int hashCode() {
            return token.hashCode();
        }
    }

    private final Authenticator<String, User> authenticator = new Authenticator<String, User>() {
        @Override
        public Optional<User> authenticate(String credentials) throws AuthenticationException {
            if ("good".equals(credentials)) {
                return Optional.of(new User(credentials));
            }

            if ("bad".equals(credentials)) {
                throw new AuthenticationException("OH NOE");
            }

            return Optional.absent();
        }
    };

    private final OAuthInjectable<User> required = new OAuthInjectable<User>(authenticator,
                                                                             "Realm",
                                                                             true);

    private final OAuthInjectable<User> optional = new OAuthInjectable<User>(authenticator,
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
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Barer WAUGH");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithBadSchemeReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Barer WAUGH");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithNoSchemeReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Barer_WAUGH");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithNoSchemeReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Barer_WAUGH");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithBadCredsReturnsUnauthorized() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Bearer WAUGH");

        try {
            required.getValue(context);
            fail("should have thrown a WebApplicationException but didn't");
        } catch (WebApplicationException e) {
            assertUnauthorized(e);
        }
    }

    @Test
    public void optionalAuthWithBadCredsReturnsNull() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Bearer WAUGH");

        assertThat(optional.getValue(context),
                   is(nullValue()));
    }

    @Test
    public void requiredAuthWithGoodCredsReturnsAUser() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Bearer good");
        
        assertThat(required.getValue(context),
                   is(new User("good")));
    }

    @Test
    public void optionalAuthWithGoodCredsReturnsAUser() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Bearer good");

        assertThat(optional.getValue(context),
                   is(new User("good")));
    }

    @Test
    public void authenticatorFailureReturnsInternalServerError() throws Exception {
        when(requestContext.getHeaderValue("Authorization")).thenReturn("Bearer bad");

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
                   is("Bearer realm=\"Realm\""));

        assertThat(response.getMetadata().getFirst("Content-Type").toString(),
                   is("text/plain"));

        assertThat(response.getEntity().toString(),
                   is("Credentials are required to access this resource."));
    }
}
