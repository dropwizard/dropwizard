package io.dropwizard.jersey.sessions;

import javax.servlet.http.HttpSession;
import java.util.Optional;

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
        return Optional.ofNullable(value);
    }

    public void set(T value) {
        session.setAttribute(ATTRIBUTE, value);
    }
}
