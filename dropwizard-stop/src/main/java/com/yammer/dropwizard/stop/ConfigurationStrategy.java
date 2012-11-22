package com.yammer.dropwizard.stop;

import com.yammer.dropwizard.config.Configuration;

/**
 * Establish the configuration strategy for the Stop configuration.
 */
public interface ConfigurationStrategy<T extends Configuration> {
  StopConfiguration getStopConfiguration(T configuration);
}
