package io.dropwizard.jersey.sessions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class FlashFactory {
    private final HttpServletRequest request;

    @Inject
    public FlashFactory(HttpServletRequest request) {
        this.request = request;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public Flash<?> provide(boolean doNotCreate) {
        if (request == null) {
            return null;
        }

        final HttpSession session = request.getSession(!doNotCreate);
        if (session != null) {
            return new Flash(session);
        }

        return null;
    }
}
