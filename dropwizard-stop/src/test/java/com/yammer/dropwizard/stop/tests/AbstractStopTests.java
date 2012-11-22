package com.yammer.dropwizard.stop.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.stop.StopConfiguration;

import javax.validation.constraints.NotNull;

/**
 * Contains common test step.
 */
public class AbstractStopTests {
  protected class TestMeService extends Service<TestMeConfiguration> {
    @Override
    public void initialize(Bootstrap<TestMeConfiguration> testMeConfigurationBootstrap) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void run(TestMeConfiguration configuration, Environment environment) throws Exception {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }

  protected class TestMeConfiguration extends Configuration {
    @NotNull
    @JsonProperty
    private String myValue;

    @NotNull
    @JsonProperty("stop")
    private StopConfiguration stopConfiguration = new StopConfiguration();

    public String getMyValue() {
      return myValue;
    }

    public void setMyValue(String myValue) {
      this.myValue = myValue;
    }

    public StopConfiguration getStopConfiguration() {
      return stopConfiguration;
    }

    public void setStopConfiguration(StopConfiguration stopConfiguration) {
      this.stopConfiguration = stopConfiguration;
    }
  }

}
