package com.codahale.dropwizard.jersey.setup;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class JerseyContainerHolder {
    private ServletContainer container;

    public JerseyContainerHolder(ServletContainer container) {
        this.container = container;
    }

    public ServletContainer getContainer() {
        return container;
    }

    public void setContainer(ServletContainer container) {
        this.container = container;
    }
}
