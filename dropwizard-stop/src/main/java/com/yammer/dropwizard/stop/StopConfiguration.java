package com.yammer.dropwizard.stop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.validation.DurationRange;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * The configuration for the Stop command.
 * {@link StopMonitor} and {@link StopCommand} both use this configuration to communicate the sequence of
 * events needed to gracefully stop the server.
 */
public class StopConfiguration {
  @JsonProperty
  private int port = 8181;

  @NotNull
  @JsonProperty
  private String key = "server-stop-me";

  @DurationRange
  @JsonProperty
  private Duration wait = Duration.seconds(2);

  /**
   * The port that the stop command is executed on.  The {@link StopMonitor} listens on this port
   * and the {@link StopCommand} issues the stop command on to this port.
   * @return port
   */
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  /**
   * The key that is used to authorize the stop command.  If the {@link StopMonitor} is configured
   * with a different key than what is used to issue the stop command the stop command is ignored.
   * @return a String representing the key
   */
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * The {@link Duration} to wait during execution of the stop sequence.
   * The {@link Duration} that the {@link StopCommand} should wait before aborting the stop command.
   * The {@link Duration} that the {@link StopMonitor} should wait before exiting the application after receiving
   * the stop command.  This duration should be at least as large as
   * {@link com.yammer.dropwizard.config.HttpConfiguration#getShutdownGracePeriod()}.
   * <p/>
   * Note this is something that could be improved so that this configuration was the exact same
   * as {@link com.yammer.dropwizard.config.HttpConfiguration#getShutdownGracePeriod()}.
   * @return {@link Duration}
   */
  public Duration getWait() {
    return wait;
  }

  public void setWait(Duration wait) {
    this.wait = wait;
  }

  @ValidationMethod(message = "Port must be in the valid range. =>1025 && <= MAX_VALUE.  i.e. not zero.")
  public boolean isPortRangeCorrect() {
    // @PortRange validation accepts 0 as a valid port.  For the Stop Port that is not a good idea.
    return port > 0 && port >= 1025;
  }
}
