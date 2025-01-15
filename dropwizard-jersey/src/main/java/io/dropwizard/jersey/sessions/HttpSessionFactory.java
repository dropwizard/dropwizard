package io.dropwizard.jersey.sessions;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;

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
