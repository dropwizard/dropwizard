package io.dropwizard.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AuthorizationException extends WebApplicationException {
    public AuthorizationException(Response response) {
        super(response);
    }
}
