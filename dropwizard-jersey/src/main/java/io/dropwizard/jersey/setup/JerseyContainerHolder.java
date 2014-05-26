package io.dropwizard.jersey.setup;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import javax.servlet.Servlet;

public class JerseyContainerHolder {
    private Servlet container;

    public JerseyContainerHolder(Servlet container) {
        this.container = container;
    }

    public Servlet getContainer() {
        return container;
    }

    public void setContainer(Servlet container) {
        this.container = container;
    }
}
