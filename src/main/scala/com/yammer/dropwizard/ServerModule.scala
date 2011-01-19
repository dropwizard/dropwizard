package com.yammer.dropwizard

import com.codahale.fig.Configuration
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.server.bio.SocketConnector
import com.google.inject.{Injector, Singleton, Provides, AbstractModule}
import org.eclipse.jetty.server.handler.{HandlerCollection, RequestLogHandler}
import com.yammer.metrics.core.MetricsServlet
import com.yammer.metrics.jetty.InstrumentedHandler
import org.eclipse.jetty.{server => jetty}
import org.eclipse.jetty.server.Connector
import com.codahale.logula.Logging
import org.eclipse.jetty.servlet.{DefaultServlet, FilterMapping, ServletContextHandler, ServletHolder}
import com.google.inject.servlet.{GuiceServletContextListener, GuiceFilter}

/**
 *
 * @author coda
 */
class ServerModule extends AbstractModule with Logging {
  def configure = {}

  @Provides
  @Singleton
  def provideServer(config: Configuration, injector: Injector): jetty.Server = {
    val server = makeServer(config, mainConnector(config), internalConnector(config))

    val handlers = new HandlerCollection
    handlers.addHandler(new InstrumentedHandler(servletContext(injector)))
    handlers.addHandler(internalServletContext)
    if (config("request_log.enabled").or(false)) {
      handlers.addHandler(injector.getInstance(classOf[RequestLogHandler]))
    }
    server.setHandler(handlers)

    server
  }

  private def mainConnector(config: Configuration) = {
    val port = config("http.port").or(8080)
    log.debug("Creating main SocketConnector on port %d", port)
    val connector = new SocketConnector
    config("http.hostname").asOption[String].foreach(connector.setHost)
    connector.setForwarded(config("http.forwarded").or(false))
    connector.setPort(port)
    connector.setName("main")
    connector
  }

  private def makeServer(config: Configuration, connectors: Connector*) = {
    val server = new jetty.Server
    connectors.foreach(server.addConnector)
    server.setSendServerVersion(false)
    server.setThreadPool(new QueuedThreadPool(config("http.max_connections").or(50)))
    server.setStopAtShutdown(true)
    server.setGracefulShutdown(config("http.shutdown_milliseconds").or(2000))
    server
  }

  private def internalConnector(config: Configuration) = {
    val connector = new SocketConnector
    connector.setPort(config("metrics.http_port").or(8081))
    connector.setName("internal")
    connector
  }

  private def servletContext(injector: Injector) = {
    val context = new ServletContextHandler()
    context.addFilter(classOf[GuiceFilter], "/*", FilterMapping.DEFAULT)
    context.addEventListener(new GuiceServletContextListener {
      def getInjector = injector
    })
    context.addServlet(classOf[DefaultServlet], "/")
    context.setConnectorNames(Array("main"))
    context
  }

  private def internalServletContext = {
    val internalContext = new ServletContextHandler()
    internalContext.addServlet(new ServletHolder(new MetricsServlet), "/*")
    internalContext.setConnectorNames(Array("internal"))
    internalContext
  }
}
