package io.dropwizard.auth;

import jakarta.ws.rs.core.Response;

public interface UnauthorizedHandler {
    Response buildResponse(String prefix, String realm);
}
