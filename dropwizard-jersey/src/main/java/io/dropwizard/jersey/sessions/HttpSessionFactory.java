package io.dropwizard.jersey.sessions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

public final class HttpSessionFactory extends AbstractContainerRequestValueFactory<HttpSession> {
    @Context
    private HttpServletRequest request;
    private boolean doNotCreate;

    public HttpSessionFactory(boolean doNotCreate) {
        this.doNotCreate = doNotCreate;
    }

    public HttpSession provide() {
        if (request == null) {
            return null;
        }

        return request.getSession(!doNotCreate);
    }
}
