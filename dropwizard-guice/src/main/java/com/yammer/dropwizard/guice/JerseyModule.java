package com.yammer.dropwizard.guice;

import com.sun.jersey.guice.JerseyServletModule;

public class JerseyModule extends JerseyServletModule {
	private final GuiceContainer container;

	public JerseyModule(GuiceContainer container) {
		this.container = container;
	}

	@Override
	protected void configureServlets() {
		bind(GuiceContainer.class).toInstance(container);
	}
}
