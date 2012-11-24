package com.yammer.dropwizard.stop;

import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Establish a 'stop' monitor on the configured port.  When a stop is issued, and the 'key' is provided then
 * attempt to stop the server, waiting for the allocated wait time in milliseconds, at a minimum.
 */
public class StopMonitor extends Thread implements ServerLifecycleListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(StopMonitor.class);

  private LifeCycle server;
  private final StopConfiguration stopConfiguration;

  private ServerSocket serverSocket;
  private ServerLifecycleListener serverListener;

  /**
   * Construct a monitor for the 'stop' command.
   * @param serverListener the current serverListener or null if there is none.  Allow for daisy chaining, if needed.
   * @param stopConfiguration that should be used.
   */
  public StopMonitor(ServerLifecycleListener serverListener, StopConfiguration stopConfiguration) {
    this.serverListener = serverListener;
    this.stopConfiguration = stopConfiguration;
  }

  @Override
  public void serverStarted(Server server) {
    if (server == null) {
      throw new IllegalStateException("Expected Server to be non-null!");
    }
    setServer(server);
    init();

    // Daisy chained?
    if (serverListener != null) {
      serverListener.serverStarted(server);
    }
  }

  private void init() {
    int port = stopConfiguration.getPort();
    try {
      if (port < 0) {
        LOGGER.info("No Stop Monitor port specified, so no monitoring thread will be started.");
        return;
      }
      // don't start the thread as a daemon.  This is because the server is a non-daemon, and if it is the only
      // daemon than the process exists without finishing the 'stop' command.
//            setDaemon(true);
      setName("StopMonitor");
      serverSocket = createSocketServer(port);
      if (port == 0) {
        port = serverSocket.getLocalPort();
        LOGGER.info("Stop monitor on port " + port + " This is not configured and " +
          "therefore is not going to work.  In that when the 'stop' is issued it will not be sent to " +
          "the right port, unless configured to match the dynamically generated port assignment. " +
          "The StopConfiguration needs a validation that works, as port 0 is not valid.");
      }
    }
    catch (Exception e) {
      LOGGER.error("Error binding stop monitor to port " + port + " and will abort monitoring. ", e);
      return;
    }

    if (serverSocket != null) {
      start();
    }
    else {
      LOGGER.warn("Not listening on stop monitor port: " + port);
    }
  }

  // Make a seam so that the class can be tested.
  protected ServerSocket createSocketServer(int port) throws IOException {
    return new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
  }

  public LifeCycle getServer() {
    return server;
  }

  public void setServer(LifeCycle server) {
    this.server = server;
  }

  @Override
  public void run() {
    String cmd = "";
    while (true) {
      Socket socket = null;
      try {
        socket = this.serverSocket.accept();

        LineNumberReader line =
          new LineNumberReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        String key = line.readLine();
        if (!this.stopConfiguration.getKey().equals(key)) {
          LOGGER.info("Ignoring stop command with incorrect key.  Check to make sure when the server was " +
            "started that the configuration used is the same now to stop.");
          continue;
        }

        cmd = line.readLine();
        if ("stop".equals(cmd)) {
          issueStop(getServer());

          socket.getOutputStream().write("Stopped\r\n".getBytes(Charset.forName("UTF-8")));
          socket.getOutputStream().flush();
          try {
            socket.close();
            socket = null;
          }
          catch (Exception e) {
            LOGGER.debug("Error trying to close the socket.  Continue anyway.", e);
          }

          try {
            this.serverSocket.close();
          }
          catch (Exception e) {
            LOGGER.debug("Error trying to close the socket.  Continue anyway.", e);
          }

          LOGGER.info("Server is running=" + server.isRunning());
          exitNow();
          break;
        }
        else if ("status".equals(cmd)) {
          if (server.isRunning() || server.isStarting() || server.isStarted()) {
            socket.getOutputStream().write("OK\r\n".getBytes(Charset.forName("UTF-8")));
            socket.getOutputStream().flush();
          }
          else {
            socket.getOutputStream().write("NOT OK\r\n".getBytes(Charset.forName("UTF-8")));
            socket.getOutputStream().flush();
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Error occurred trying to issue command=" + cmd, e);
      }
      finally {
        if (socket != null) {
          try {
            socket.close();
            socket = null;
          }
          catch (Exception e) {
            LOGGER.debug("Error trying to close the socket.  Continue anyway.", e);
          }
        }
      }
    }
  }

  // add a seam for testing.  I.e. wouldn't be good to actually exit.
  protected void exitNow() {
    System.exit(0);
  }

  private void issueStop(final LifeCycle server1) throws Exception {
    if (server1 != null) {
      try {
        int wait = stopConfiguration.getWait();
        if (wait < 0) {
          wait = 0;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        //  Stop the server in another thread, so that the stop process can be monitored.
        Thread stopThread = new Thread("Stopping Server") {
          @Override
          public void run() {
            try {
              server1.stop();
            }
            catch (Exception e) {
              LOGGER.info("Error occurred while trying to stop the server.  " +
                "Ignoring the error for now, as there is likely nothing that " +
                "can be done if we stopped.", e);
            }
            finally {
              countDownLatch.countDown();
            }
          }
        };
        stopThread.start();

        if (countDownLatch.await(wait, TimeUnit.SECONDS)) {
          LOGGER.info("Server should be stopped now.");
        }
      }
      catch (InterruptedException e) {
        LOGGER.info("Interrupted waiting for server to be terminated.  Will exit now.");
      }
    }
  }
}
