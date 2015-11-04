package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HealthFactory {
  @Valid
  @NotNull
  private ExecutorServiceFactory executorService = new ManagedExecutorServiceFactory("TimeBoundHealthCheck-pool-%d");

  @JsonProperty("executorService")
  public ExecutorServiceFactory getExecutorService() {
    return executorService;
  }

  @JsonProperty("executorService")
  public void setExecutorService(ExecutorServiceFactory executorService) {
    this.executorService = executorService;
  }
}