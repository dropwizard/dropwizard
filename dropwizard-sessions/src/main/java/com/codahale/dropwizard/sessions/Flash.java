package com.codahale.dropwizard.sessions;

import com.google.common.base.Optional;

import javax.servlet.http.HttpSession;

public class Flash<T> {
    private static final String ATTRIBUTE = "flash";
    private final HttpSession session;
    private final T value;

    @SuppressWarnings("unchecked")
    Flash(HttpSession session) {
        this.session = session;
        this.value = (T) session.getAttribute(ATTRIBUTE);
        if (this.value != null) {
            session.removeAttribute(ATTRIBUTE);
        }
    }

    public Optional<T> get() {
        return Optional.fromNullable(value);
    }

    public void set(T value) {
        session.setAttribute(ATTRIBUTE, value);
    }
}
