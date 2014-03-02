package io.dropwizard.auth.basic;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * A Jersey provider for Basic HTTP authentication.
 *
 * @param <T> the principal type.
 */

public final class BasicAuthFactory<T> extends AuthFactory<BasicCredentials,T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFactory.class);
    private static final String PREFIX = "Basic";
    private static final String CHALLENGE_FORMAT = PREFIX + " realm=\"%s\"";
    
    @Context
    private HttpServletRequest request;
    
    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm;
    
    public BasicAuthFactory (Authenticator<BasicCredentials,T> authenticator,
                             String realm,
                             Class<T> generatedClass)
    {
        super (authenticator);
        this.required = false;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }
    
    private BasicAuthFactory(boolean required,
                            Authenticator<BasicCredentials,T> authenticator,
                            String realm,
                            Class<T> generatedClass)
    {
        super (authenticator);
        this.required = required;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }
    
    @Override
    public AuthFactory<BasicCredentials,T> clone(boolean required)
    {
        BasicAuthFactory<T> factory = new BasicAuthFactory<> (required, 
                authenticator(), this.realm, this.generatedClass);
        return factory;
    }
    
    public T provide() {
        T principal = null;

        if (request != null)
        {
            final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            try {
                if (header != null) {
                    final int space = header.indexOf(' ');
                    if (space > 0) {
                        final String method = header.substring(0, space);
                        if (PREFIX.equalsIgnoreCase(method)) {
                            final String decoded = B64Code.decode(header.substring(space + 1),
                                    StringUtil.__ISO_8859_1);
                            final int i = decoded.indexOf(':');
                            if (i > 0) {
                                final String username = decoded.substring(0, i);
                                final String password = decoded.substring(i + 1);
                                final BasicCredentials credentials = new BasicCredentials(username,
                                        password);
                                final Optional<T> result = authenticator().authenticate(credentials);
                                if (result.isPresent()) {
                                    return result.get();
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        if (principal == null && this.required)
        {
            final String challenge = String.format(CHALLENGE_FORMAT, realm);
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE,
                            challenge)
                            .entity("Credentials are required to access this resource.")
                            .type(MediaType.TEXT_PLAIN_TYPE)
                            .build());
        }
        return null;
    }
        
    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}
