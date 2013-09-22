package io.dropwizard.lifecycle;

import org.eclipse.jetty.util.component.AbstractLifeCycle;

/**
 * A wrapper for {@link Managed} instances which ties them to a Jetty {@link
 * org.eclipse.jetty.util.component.LifeCycle}.
 */
public class JettyManaged extends AbstractLifeCycle implements Managed {
    private final Managed managed;

    /**
     * Creates a new JettyManaged wrapping {@code managed}.
     *
     * @param managed a {@link Managed} instance to be wrapped
     */
    public JettyManaged(Managed managed) {
        this.managed = managed;
    }

    public Managed getManaged() {
        return managed;
    }

    @Override
    protected void doStart() throws Exception {
        managed.start();
    }

    @Override
    protected void doStop() throws Exception {
        managed.stop();
    }

    @Override
    public String toString() {
        return managed.toString();
    }
}
