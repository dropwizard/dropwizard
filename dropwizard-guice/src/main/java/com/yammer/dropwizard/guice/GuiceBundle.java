package com.yammer.dropwizard.guice;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public abstract class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

	@Override
	public void initialize(Bootstrap<?> bootstrap) {

	}

	protected abstract Collection<? extends Module> configureModules(T configuration);

	@Override
	public void run(T configuration, final Environment environment) {
		@SuppressWarnings("serial")
		GuiceContainer container = new GuiceContainer() {
			protected ResourceConfig getDefaultResourceConfig(
					Map<String, Object> props, WebConfig webConfig)
					throws javax.servlet.ServletException {
				return environment.getJerseyResourceConfig();
			};
		};
		environment.setJerseyServletContainer(container);
		environment.addFilter(GuiceFilter.class, configuration.getHttpConfiguration().getRootPath());

		Collection<Module> modules = Lists.newArrayList();
		modules.add(Modules.override(new JerseyServletModule()).with(new JerseyContainerModule(container)));
		modules.addAll(configureModules(configuration));
		Guice.createInjector(modules);
	}

}
