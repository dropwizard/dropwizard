package com.yammer.dropwizard

import com.codahale.fig.Configuration
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.server.bio.SocketConnector
import com.google.inject.{Injector, Singleton, Provides, AbstractModule}
import org.eclipse.jetty.server.handler.{HandlerCollection, RequestLogHandler}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import com.yammer.metrics.core.MetricsServlet
import com.yammer.metrics.jetty.InstrumentedHandler
import org.eclipse.jetty.{server => jetty}
import com.codahale.logula.Logging

/**
 *
 * @author coda
 */
class ServerModule extends AbstractModule with Logging {
  def configure = {}

  @Provides
  @Singleton
  def provideServer(config: Configuration, injector: Injector): jetty.Server = {
    val port = config("http.port").or(8080)
    log.debug("Creating main SocketConnector on port %d", port)
    val connector = new SocketConnector
    config("http.hostname").asOption[String].foreach(connector.setHost)
    connector.setForwarded(config("http.forwarded").or(false))
    connector.setPort(port)
    connector.setName("main")

    val internalConnector = new SocketConnector
    internalConnector.setPort(config("metrics.http_port").or(8081))
    internalConnector.setName("internal")

    val server = new jetty.Server
    server.addConnector(connector)
    server.addConnector(internalConnector)
    server.setSendServerVersion(false)
    server.setThreadPool(new QueuedThreadPool(config("http.max_connections").or(50)))
    server.setStopAtShutdown(true)
    server.setGracefulShutdown(config("http.shutdown_milliseconds").or(2000))

    val handlers = new HandlerCollection

    val context = new ServletContextHandler()
    context.addServlet(new ServletHolder(injector.getInstance(classOf[ScanningGuiceContainer])), "/*")
    context.setConnectorNames(Array("main"))
    handlers.addHandler(new InstrumentedHandler(context))

    val internalContext = new ServletContextHandler()
    internalContext.addServlet(new ServletHolder(new MetricsServlet), "/*")
    internalContext.setConnectorNames(Array("internal"))
    handlers.addHandler(internalContext)

    if (config("request_log.enabled").or(false)) {
      handlers.addHandler(injector.getInstance(classOf[RequestLogHandler]))
    }

    server.setHandler(handlers)

    server
  }
}
