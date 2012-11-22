package com.yammer.dropwizard.stop.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import com.yammer.dropwizard.stop.StopBundle;
import com.yammer.dropwizard.stop.StopConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.constraints.NotNull;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test the StopBundle class.
 */
public class StopBundleTest extends AbstractStopTests {
  private StopBundle fixture;

  @Before
  public void setUp() throws Exception {
    fixture = new StopBundle<TestMeConfiguration>() {
      @Override
      public StopConfiguration getStopConfiguration(TestMeConfiguration configuration) {
        return configuration.getStopConfiguration();
      }
    };
  }

  @Test
  public void initialize() {
    Bootstrap bootstrap = new Bootstrap<TestMeConfiguration>(new TestMeService());
    fixture.initialize(bootstrap);

    ImmutableList commands = bootstrap.getCommands();
    assertThat(commands.size()).isEqualTo(1);
  }

  @Test
  public void run() throws Exception {
    Environment environment = mock(Environment.class);
    fixture.run(new TestMeConfiguration(), environment);

    ArgumentCaptor<ServerLifecycleListener> captor = ArgumentCaptor.forClass(ServerLifecycleListener.class);

    verify(environment).setServerLifecycleListener(captor.capture());
    ServerLifecycleListener serverLifecycleListener = captor.getValue();
    assertThat(serverLifecycleListener).isNotNull();
  }
}
