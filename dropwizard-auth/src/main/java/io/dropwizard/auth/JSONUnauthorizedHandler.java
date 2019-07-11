package io.dropwizard.auth;

import io.dropwizard.jersey.errors.ErrorMessage;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JSONUnauthorizedHandler implements UnauthorizedHandler {
    private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";

    @Override
    public Response buildResponse(String prefix, String realm) {
        ErrorMessage errorMessage = new ErrorMessage(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            "Credentials are required to access this resource."
        );
        return Response.status(errorMessage.getCode())
            .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, prefix, realm))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(errorMessage)
            .build();
    }
}
