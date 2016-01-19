package io.dropwizard.auth;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.auth.principal.NullPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;

public class AuthFilterTest {

    @Test
    public void isSecureShouldStayTheSame() throws Exception {
        ContainerRequestContext requestContext = new FakeSecureRequestContext();

        new SimpleAuthFilter().filter(requestContext);

        assertTrue(requestContext.getSecurityContext().isSecure());
    }

    static class SimpleAuthFilter extends AuthFilter<String, Principal> {

        SimpleAuthFilter() {
            authenticator = credentials -> Optional.of(new NullPrincipal());
            authorizer = new PermitAllAuthorizer<>();
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            authenticate(requestContext, "some-password", "SOME_SCHEME");
        }
    }

    static class FakeSecureRequestContext implements ContainerRequestContext {

        private SecurityContext securityContext;

        FakeSecureRequestContext() {
            securityContext = mock(SecurityContext.class);
            when(securityContext.isSecure()).thenReturn(true);
        }

        @Override
        public SecurityContext getSecurityContext() {
            return securityContext;
        }

        @Override
        public void setSecurityContext(SecurityContext context) {
            this.securityContext = context;
        }

        @Override
        public Object getProperty(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getPropertyNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setProperty(String name, Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeProperty(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UriInfo getUriInfo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRequestUri(URI requestUri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRequestUri(URI baseUri, URI requestUri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Request getRequest() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMethod(String method) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeaderString(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLanguage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLength() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MediaType getMediaType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Locale> getAcceptableLanguages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Cookie> getCookies() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasEntity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getEntityStream() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEntityStream(InputStream input) {
            throw new UnsupportedOperationException();
        }


        @Override
        public void abortWith(Response response) {
            throw new UnsupportedOperationException();
        }
    }
}
