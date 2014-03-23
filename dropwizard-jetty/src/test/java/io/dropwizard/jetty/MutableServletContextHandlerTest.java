package io.dropwizard.jetty;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutableServletContextHandlerTest {
    private final MutableServletContextHandler handler = new MutableServletContextHandler();

    @Test
    public void defaultsToSessionsBeingDisabled() throws Exception {
        assertThat(handler.isSessionsEnabled())
                .isFalse();
    }

    @Test
    public void defaultsToSecurityBeingDisabled() throws Exception {
        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    public void canEnableSessionManagement() throws Exception {
        handler.setSessionsEnabled(true);

        assertThat(handler.isSessionsEnabled())
                .isTrue();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    public void canDisableSessionManagement() throws Exception {
        handler.setSessionsEnabled(true);
        handler.setSessionsEnabled(false);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }

    @Test
    public void canEnableSecurity() throws Exception {
        handler.setSecurityEnabled(true);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isTrue();
    }

    @Test
    public void canDisableSecurity() throws Exception {
        handler.setSecurityEnabled(true);
        handler.setSecurityEnabled(false);

        assertThat(handler.isSessionsEnabled())
                .isFalse();

        assertThat(handler.isSecurityEnabled())
                .isFalse();
    }
}
