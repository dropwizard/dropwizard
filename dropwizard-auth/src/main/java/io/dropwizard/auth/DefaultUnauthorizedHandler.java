package io.dropwizard.auth;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class DefaultUnauthorizedHandler implements UnauthorizedHandler {
    private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";

    @Override
    public Response buildResponse(String prefix, String realm) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, prefix, realm))
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Credentials are required to access this resource.")
                .build();
    }
}
