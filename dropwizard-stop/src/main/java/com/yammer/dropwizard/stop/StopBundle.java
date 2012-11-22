package com.yammer.dropwizard.stop;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.util.Generics;

/**
 * The {@link StopBundle} groups functionality to stop the server via commands.
 * Without these commands the server must always be stopped with a SIGINT.
 * Simply add this bundle to your Service during initialization.  Additionally,
 * you must 'initialize' this bundle too, as currently dropwizard-core.Bootstrap does not
 * initialize 'configured bundles' but does on non-configured bundles.
 *
 */
public abstract class StopBundle <T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T> {
  @Override
  public final void initialize(Bootstrap<?> bootstrap) {
      final Class<T> klass = Generics.getTypeParameter(getClass(), Configuration.class);
      bootstrap.addCommand(new StopCommand<T>(this, klass));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
    final StopConfiguration stopConfig = getStopConfiguration(configuration);
    environment.setServerLifecycleListener(new StopMonitor(environment.getServerListener(), stopConfig));
  }
}
