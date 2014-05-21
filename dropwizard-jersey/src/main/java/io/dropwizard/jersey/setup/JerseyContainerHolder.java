package io.dropwizard.jersey.setup;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import javax.servlet.Servlet;

public class JerseyContainerHolder {
    private Servlet container;

    public JerseyContainerHolder(Servlet container) {
        this.container = container;
    }

    public ServletContainer getContainer() {
        if (container instanceof ServletContainer) {
            return (ServletContainer) container;
        }
        throw new RuntimeException("Not a ServletContainer. Use getServlet instead.");
    }

    public void setContainer(Servlet container) {
        this.container = container;
    }

    public Servlet getServlet() {
        return container;
    }
}
