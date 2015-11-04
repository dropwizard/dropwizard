package io.dropwizard.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validator;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthFactoryTest {
  private HealthFactory health;
  private final ObjectMapper objectMapper = Jackson.newObjectMapper();
  private final Validator validator = BaseValidator.newValidator();

  @Before
  public void setUp() throws Exception {
    objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
        FileAppenderFactory.class, SyslogAppenderFactory.class, ExecutorServiceFactory.class);
    File ymlFile = new File(Resources.getResource("yaml/health.yml").toURI());
    health = new ConfigurationFactory<>(HealthFactory.class, validator, objectMapper, "dw")
        .build(ymlFile);
  }

  @Test
  public void isDiscoverable() throws Exception {
    assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
        .contains(ManagedExecutorServiceFactory.class);
  }

  @Test
  public void testExecutorServiceNameFormat() {
    assertThat(getManagedExecutorService().getNameFormat()).isEqualTo("for-testing-pool-%d");
  }

  @Test
  public void testExecutorServiceWorkQueueCapacity() {
    assertThat(getManagedExecutorService().getWorkQueueCapacity()).isEqualTo(2);
  }

  @Test
  public void testExecutorServiceMinThreads() {
    assertThat(getManagedExecutorService().getMinThreads()).isEqualTo(3);
  }

  @Test
  public void testExecutorServiceMaxThreads() {
    assertThat(getManagedExecutorService().getMaxThreads()).isEqualTo(6);
  }

  @Test
  public void testExecutorServiceBuild() throws Exception {
    LifecycleEnvironment lifecycle = new LifecycleEnvironment();
    ExecutorService executorService = health.getExecutorService().build(lifecycle);
    try {
      assertThat(executorService.isShutdown()).isFalse();
      assertThat(executorService.isTerminated()).isFalse();
      assertThat(lifecycle.getManagedObjects().isEmpty()).isFalse();
      Iterator<LifeCycle> managedObjectsIterator = lifecycle.getManagedObjects().iterator();
      while (managedObjectsIterator.hasNext()) {
        managedObjectsIterator.next().start();
      }
      managedObjectsIterator = lifecycle.getManagedObjects().iterator();
      while (managedObjectsIterator.hasNext()) {
        managedObjectsIterator.next().stop();
      }
      assertThat(executorService.isShutdown()).isTrue();
      assertThat(executorService.isTerminated()).isTrue();
    } finally {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }
  }

  private ManagedExecutorServiceFactory getManagedExecutorService() {
    assertThat(health.getExecutorService() instanceof ManagedExecutorServiceFactory);
    return (ManagedExecutorServiceFactory)health.getExecutorService();
  }
}
