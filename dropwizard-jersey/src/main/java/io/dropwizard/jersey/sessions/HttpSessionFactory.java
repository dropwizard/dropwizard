package io.dropwizard.jersey.sessions;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

public final class HttpSessionFactory extends AbstractContainerRequestValueFactory<HttpSession> {
    @Context
    @Nullable
    private HttpServletRequest request;
    private boolean doNotCreate;

    public HttpSessionFactory(boolean doNotCreate) {
        this.doNotCreate = doNotCreate;
    }

    @Override
    @Nullable
    public HttpSession provide() {
        if (request == null) {
            return null;
        }

        return request.getSession(!doNotCreate);
    }
}
