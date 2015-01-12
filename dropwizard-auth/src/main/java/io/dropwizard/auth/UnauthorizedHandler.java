package io.dropwizard.auth;

import javax.ws.rs.core.Response;

public interface UnauthorizedHandler {
    Response buildResponse(String prefix, String realm);
}
