package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;

import java.util.concurrent.ExecutorService;

/**
 * A factory for building {@link java.util.concurrent.ExecutorService} instances for Dropwizard applications.
 *
 * @see io.dropwizard.health.ManagedExecutorServiceFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ManagedExecutorServiceFactory.class)
public interface ExecutorServiceFactory extends Discoverable{
  /**
   * Build an executorService for the given Dropwizard application.
   *
   * @param environment the application's environment
   * @return a {@link java.util.concurrent.ExecutorService} running used by the Dropwizard application
   */
  ExecutorService build(LifecycleEnvironment environment);
}
