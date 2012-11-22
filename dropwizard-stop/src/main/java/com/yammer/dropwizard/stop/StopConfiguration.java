package com.yammer.dropwizard.stop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.validation.DurationRange;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

/**
 * The configuration for the Stop command.
 */
public class StopConfiguration {
  @JsonProperty
  private int port = 8181;

  @NotNull
  @JsonProperty
  private String key = "server-stop-me";

  @DurationRange
  @JsonProperty
  private int wait = 0;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public int getWait() {
    return wait;
  }

  public void setWait(int wait) {
    this.wait = wait;
  }

  @ValidationMethod(message = "Port must be in the valid range. >1025 && < MAX_VALUE.  i.e. not zero.")
  public boolean isPortRangeCorrect() {
    int maxValue = Integer.MAX_VALUE;
    return port > 0 && port >= 1025 && port <= maxValue;
  }
}
