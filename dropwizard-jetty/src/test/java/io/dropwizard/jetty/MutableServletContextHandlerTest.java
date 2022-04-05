package io.dropwizard.jetty;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutableServletContextHandlerTest {
    private final MutableServletContextHandler handler = new MutableServletContextHandler();

    @Test
    void defaultsToSessionsBeingDisabled() throws Exception {
        assertThat(handler.isSessionsEnabled())
                .isFalse();
    }

    @Test
    void defaultsToSecurityBeingDisabled() throws Exception {
        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    void canEnableSessionManagement() throws Exception {
        handler.setSessionsEnabled(true);

        assertThat(handler.isSessionsEnabled())
                .isTrue();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    void canDisableSessionManagement() throws Exception {
        handler.setSessionsEnabled(true);
        handler.setSessionsEnabled(false);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    void canEnableSecurity() throws Exception {
        handler.setSecurityEnabled(true);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isTrue();
    }

    @Test
    void canDisableSecurity() throws Exception {
        handler.setSecurityEnabled(true);
        handler.setSecurityEnabled(false);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }
}
