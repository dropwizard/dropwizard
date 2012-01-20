package com.yammer.dropwizard;

import com.yammer.dropwizard.config.*;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.validation.Validator;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.eclipse.jetty.server.Server;
import java.io.File;

/**
 * ServiceDaemon runner for Dropwizard Services.
 *
 * Create a Service and Configuration class as normal, and create a ServiceDaemon
 * implementation that creates an instance of your service:
 * <code>
 *   public class MyServiceDaemon extends ServiceDaemon<MyConfiguration> {
 *     public MyServiceDaemon() {
 *       super(new MyService("MyService"));
 *     }
 *   }
 * </code>
 *
 * You may then run your service using either of the Apache Commons Daemon
 * wrappers (jsvc on UNIX or procrun on Win32):
 * <pre>
 *   $ jsvc -pidfile /var/run/myservice.pid MyServiceDaemon # start your service
 *   $ jsvc -pidfile /var/run/myservice.pid -stop # stop your service
 * </pre>
 *
 * @see <a href="http://commons.apache.org/daemon/">Apache Commons Daemon</a>
 */
public abstract class ServiceDaemon<T extends Configuration> implements Daemon {
  private Log log = Log.forClass(this.getClass());

  private ConfigurationFactory<T> factory;
  private Server server;
  private String[] args;
  private Service<T> service;

  protected ServiceDaemon(Service<T> service) {
    this.service = service;
  }

  @Override
  public void init(DaemonContext context) throws Exception {
    factory  = new ConfigurationFactory<T>(service.getConfigurationClass(), new Validator());
    args = context.getArguments();
  }

  @Override
  public void start() throws Exception {
    T configuration = factory.build(new File(args[0]));
    new LoggingFactory(configuration.getLoggingConfiguration()).configure();

    Environment env = new Environment(configuration);
    service.initializeWithBundles(configuration, env);

    server = new ServerFactory(configuration.getHttpConfiguration()).buildServer(env);

    log.info("Starting {}", service.getName());
    try {
      server.start();
      server.join();
    } catch (Exception e) {
      log.error(e, "Unable to start server, shutting down");
      server.stop();
      throw e;
    }
  }

  @Override
  public void stop() throws Exception {
    server.stop();
  }

  @Override
  public void destroy() {

  }
}
