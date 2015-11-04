package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ManagedExecutorServiceFactory implements ExecutorServiceFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ManagedExecutorServiceFactory.class);

  public ManagedExecutorServiceFactory() {
  }
  public ManagedExecutorServiceFactory(String nameFormat) {
    this.nameFormat = nameFormat;
  }

  @NotEmpty
  private String nameFormat;

  @Min(1)
  private int workQueueCapacity = 1;

  @Min(2)
  private int maxThreads = 4;

  @Min(1)
  private int minThreads = 1;

  @JsonProperty
  public String getNameFormat() {
    return nameFormat;
  }

  @JsonProperty
  public void setNameFormat(String nameFormat) {
    this.nameFormat = nameFormat;
  }

  @JsonProperty
  public int getWorkQueueCapacity() {
    return workQueueCapacity;
  }

  @JsonProperty
  public void setWorkQueueCapacity(int workQueueCapacity) {
    this.workQueueCapacity = workQueueCapacity;
  }

  @JsonProperty
  public int getMaxThreads() {
    return maxThreads;
  }

  @JsonProperty
  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  @JsonProperty
  public int getMinThreads() {
    return minThreads;
  }

  @JsonProperty
  public void setMinThreads(int minThreads) {
    this.minThreads = minThreads;
  }

  @Override
  public ExecutorService build(LifecycleEnvironment lifecycle) {
    return lifecycle.executorService(nameFormat)
        .workQueue(new ArrayBlockingQueue<Runnable>(workQueueCapacity))
        .minThreads(minThreads)
        .maxThreads(maxThreads)
        .threadFactory(new ThreadFactoryBuilder().setDaemon(true).build())
        .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
        .build();
  }
}
