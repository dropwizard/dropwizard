package io.dropwizard.jersey.setup;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.Servlet;

public class JerseyContainerHolder {

    @Nullable
    private Servlet container;

    public JerseyContainerHolder(@Nullable Servlet container) {
        this.container = container;
    }

    @Nullable
    public Servlet getContainer() {
        return container;
    }

    public void setContainer(@Nullable Servlet container) {
        this.container = container;
    }
}
