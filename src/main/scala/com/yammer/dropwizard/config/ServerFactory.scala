package com.yammer.dropwizard.config

import com.codahale.fig.Configuration
import com.yammer.metrics.jetty.InstrumentedHandler
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.nio.{BlockingChannelConnector, SelectChannelConnector}
import org.eclipse.jetty.server.{Server, Connector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import com.yammer.metrics.reporting.MetricsServlet
import javax.servlet.Servlet
import org.eclipse.jetty.servlet._

object ServerFactory {
  def provideServer(implicit config: Configuration, servlet: Servlet) = {
    val server = makeServer(mainConnector, internalConnector)

    val handlers = new HandlerCollection
    handlers.addHandler(new InstrumentedHandler(servletContext))
    handlers.addHandler(internalServletContext)
    if (config("request_log.enabled").or(false)) {
      handlers.addHandler(RequestLogHandlerFactory.buildHandler)
    }
    server.setHandler(handlers)

    server
  }

  private def newConnector(implicit config: Configuration) =
    config("http.connector").or("blocking_channel") match {
      case "socket" => new SocketConnector
      case "select_channel" => new SelectChannelConnector
      case "blocking_channel" => new BlockingChannelConnector
    }

  private def mainConnector(implicit config: Configuration) = {
    val port = config("http.port").or(8080)
    val connector = newConnector(config)
    config("http.hostname").asOption[String].foreach(connector.setHost)
    connector.setForwarded(config("http.forwarded").or(false))
    connector.setPort(port)
    connector.setName("main")
    connector
  }

  private def makeServer(connectors: Connector*)(implicit config: Configuration) = {
    val server = new Server
    connectors.foreach(server.addConnector)
    server.setSendServerVersion(false)
    server.setThreadPool(makeThreadPool)
    server.setStopAtShutdown(true)
    server.setGracefulShutdown(config("http.shutdown_milliseconds").or(2000))
    server
  }

  private def makeThreadPool(implicit config: Configuration) = {
    val pool = new QueuedThreadPool
    config("http.max_connections").asOption[Int].foreach(pool.setMaxThreads)
    config("http.min_connections").asOption[Int].foreach(pool.setMinThreads)
    pool
  }

  private def internalConnector(implicit config: Configuration) = {
    val connector = newConnector(config)
    connector.setPort(config("metrics.port").or(8081))
    connector.setName("internal")
    connector
  }

  private def servletContext(implicit servlet: Servlet) = {
    val context = new ServletContextHandler()
    context.addServlet(new ServletHolder(servlet), "/")
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
