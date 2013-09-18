package io.dropwizard.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

public class MutableServletContextHandler extends ServletContextHandler {
    public boolean isSecurityEnabled() {
        return (this._options & SECURITY) != 0;
    }

    public void setSecurityEnabled(boolean enabled) {
        if (enabled) {
            this._options |= SECURITY;
        } else {
            this._options &= ~SECURITY;
        }
    }

    public boolean isSessionsEnabled() {
        return (this._options & SESSIONS) != 0;
    }

    public void setSessionsEnabled(boolean enabled) {
        if (enabled) {
            this._options |= SESSIONS;
        } else {
            this._options &= ~SESSIONS;
        }
    }
}
