package io.dropwizard.jersey.sessions;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.checkerframework.checker.nullness.qual.Nullable;

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
