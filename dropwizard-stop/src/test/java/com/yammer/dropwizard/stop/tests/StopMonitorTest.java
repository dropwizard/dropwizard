package com.yammer.dropwizard.stop.tests;

import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import com.yammer.dropwizard.stop.StopConfiguration;
import com.yammer.dropwizard.stop.StopMonitor;
import com.yammer.dropwizard.util.Duration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.AtLeast;
import org.mockito.internal.verification.Times;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test the StopMonitor class.
 */
public class StopMonitorTest extends AbstractStopTests implements ServerLifecycleListener {

  private ShortStopMonitor fixture;
  private ServerSocket mockServerSocket;
  private Server foundServer;
  private CountDownLatch runCountDown;
  private CountDownLatch exitCountDown;
  private StopConfiguration stopConfiguration;
  private OutputStream mockOut;
  private Socket mockSocket;
  private boolean skipRun;
  private Server mockServer;


  @Before
  public void setUp() {
    skipRun = true;
    runCountDown = new CountDownLatch(1);
    exitCountDown = new CountDownLatch(1);
    stopConfiguration = new StopConfiguration();
    mockServerSocket = mock(ServerSocket.class);
    mockSocket = mock(Socket.class);
    fixture = new ShortStopMonitor(this, stopConfiguration);
    mockServer = mock(Server.class);
  }

  @Test(expected = IllegalStateException.class)
  public void serverStartNullArg() {
    fixture.serverStarted(null);
  }

  @Test
  public void serverStarted() throws Exception {
    foundServer = null;
    fixture.serverStarted(mockServer);

    assertThat(runCountDown.await(2, TimeUnit.SECONDS)).isTrue();
    assertThat(foundServer).isEqualTo(mockServer);
    assertThat(fixture.getServer()).isEqualTo(mockServer);
  }

  @Test
  public void serverStartedNoDaisyChain() throws Exception {
    fixture = new ShortStopMonitor(null, stopConfiguration);

    fixture.serverStarted(mockServer);

    assertThat(runCountDown.await(2, TimeUnit.SECONDS)).isTrue();
    assertThat(foundServer).isNull();
    assertThat(fixture.getServer()).isEqualTo(mockServer);
  }

  @Test
  public void serverStartedNoPort() throws Exception {
    stopConfiguration.setPort(-1);

    fixture.serverStarted(mockServer);

    assertThat(runCountDown.await(200, TimeUnit.MILLISECONDS)).isFalse();
  }

  @Test
  public void serverStartedZeroPort() throws Exception {
    stopConfiguration.setPort(0);
    when(mockServerSocket.getLocalPort()).thenReturn(4000);

    fixture.serverStarted(mockServer);

    assertThat(runCountDown.await(2, TimeUnit.SECONDS)).isTrue();
    verify(mockServerSocket).getLocalPort();
  }

  @Test
  public void serverWithoutAServer() throws Exception {
    mockServerSocket = null;

    fixture.serverStarted(mockServer);

    assertThat(runCountDown.await(200, TimeUnit.MILLISECONDS)).isFalse();
  }

  /**
   * This test can be dangerous, because it tests threading aspects.
   * Ideally each part of the 'run' should be broken up into smaller, more testable parts such
   * that the thread aspects are not relevant.
   *
   * @throws Exception
   */
  @Test
  public void testRun() throws Exception {
    LifeCycle mockLifeCycle = mock(LifeCycle.class);
    stopConfiguration.setWait(Duration.seconds(8));
    byte[] badCommand = ("bad key\r\nstatus\r\n").getBytes();
    byte[] stopCommand = (stopConfiguration.getKey() + "\r\nstop\r\n").getBytes();
    byte[] statusCommand = (stopConfiguration.getKey() + "\r\nstatus\r\n").getBytes();
    // Set the status as NOT OK
    when(mockSocket.getInputStream())
      .thenReturn(new ByteArrayInputStream(statusCommand))
      .thenReturn(new ByteArrayInputStream(statusCommand))
      .thenReturn(new ByteArrayInputStream(badCommand))
      .thenReturn(new ByteArrayInputStream(stopCommand));
    when(mockLifeCycle.isRunning())
      .thenReturn(false)
      .thenReturn(true)
      .thenReturn(true)
      .thenReturn(true);
     doNothing().when(mockLifeCycle).stop();


    mockOut = mock(OutputStream.class);
    when(mockSocket.getOutputStream()).thenReturn(mockOut);
    when(mockServerSocket.accept()).thenReturn(mockSocket);

    skipRun = false;  // this will launch the thread.
    fixture.serverStarted(mockServer);
    // without this the 'stopping service' thread throws NPE.
    fixture.setServer(mockLifeCycle);

    // Have to wait for some threading aspects.  Ugly!
    assertThat(runCountDown.await(2, TimeUnit.SECONDS)).isTrue();
    Duration stopConfigurationWait = stopConfiguration.getWait();
    assertThat(exitCountDown.await(stopConfigurationWait.getQuantity(), stopConfigurationWait.getUnit())).isTrue();

    ArgumentCaptor<byte[]> captorWrite = ArgumentCaptor.forClass(byte[].class);
    verify(mockOut, new Times(3)).write(captorWrite.capture());
    List<byte[]> byteValues = captorWrite.getAllValues();
    List<String> stringValues = new ArrayList<String>();
    for (byte[] byteValue : byteValues) {
      String value = new String(byteValue);
      stringValues.add(value);
    }
    assertThat(stringValues).contains("NOT OK\r\n");
    assertThat(stringValues).contains("OK\r\n");
    assertThat(stringValues).contains("Stopped\r\n");

    verify(mockOut, new Times(3)).flush();
    verify(mockSocket, new AtLeast(1)).close();
    verify(mockLifeCycle).stop();
  }

  private class ShortStopMonitor extends StopMonitor {

    /**
     * Construct a monitor for the 'stop' command.
     *
     * @param serverListener    the current serverListener or null if there is none.  Allow for daisy chaining, if needed.
     * @param stopConfiguration that should be used.
     */
    public ShortStopMonitor(ServerLifecycleListener serverListener, StopConfiguration stopConfiguration) {
      super(serverListener, stopConfiguration);
    }

    @Override
    protected ServerSocket createSocketServer(int port) throws IOException {
      return mockServerSocket;
    }

    @Override
    public void run() {
      runCountDown.countDown();
      if (skipRun) {
      }
      else {
        super.run();
      }
    }

    @Override
    protected void exitNow() {
      // Do nothing.
      exitCountDown.countDown();
    }
  }
  @Override
  public void serverStarted(Server server) {
    this.foundServer = server;
  }
}
