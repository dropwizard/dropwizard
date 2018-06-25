package io.dropwizard.jersey.sessions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class HttpSessionFactory {
    private final HttpServletRequest request;

    @Inject
    public HttpSessionFactory(HttpServletRequest request) {
        this.request = request;
    }

    @Nullable
    public HttpSession provide(boolean doNotCreate) {
        if (request == null) {
            return null;
        }

        return request.getSession(!doNotCreate);
    }
}
