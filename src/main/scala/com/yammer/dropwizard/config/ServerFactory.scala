package com.yammer.dropwizard.config

import com.codahale.fig.Configuration
import com.yammer.metrics.jetty.InstrumentedHandler
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.nio.{BlockingChannelConnector, SelectChannelConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import com.yammer.metrics.reporting.MetricsServlet
import javax.servlet.{Filter, Servlet}
import java.util.EnumSet
import org.eclipse.jetty.server.{DispatcherType, Server, Connector}
import com.yammer.dropwizard.util.QuietErrorHandler
import org.eclipse.jetty.servlet._
import com.yammer.dropwizard.tasks.{Task, TaskServlet}

object ServerFactory {
  def provideServer(implicit config: Configuration,
                    servlets: Map[String, ServletHolder],
                    filters: Map[String, FilterHolder],
                    tasks: Set[Task]) = {
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
      case "select_channel" => {
        val connector = new SelectChannelConnector
        connector.setAcceptors(config("http.acceptor_threads").or(2))
        connector.setMaxIdleTime(config("http.max_idle_time_seconds").or(300))
        connector.setLowResourcesConnections(config("http.low_resources_connections").or(25000))
        connector.setLowResourcesMaxIdleTime(config("http.low_resources_max_idle_time_seconds").or(5))
        connector
      }
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
    server.addBean(new QuietErrorHandler)
    server.setSendServerVersion(false)
    server.setThreadPool(makeThreadPool)
    server.setStopAtShutdown(true)
    server.setGracefulShutdown(config("http.shutdown_milliseconds").or(2000))
    server
  }

  private def makeThreadPool(implicit config: Configuration) = {
    val pool = new QueuedThreadPool
    config("http.max_threads").asOption[Int].foreach(pool.setMaxThreads)
    config("http.min_threads").asOption[Int].foreach(pool.setMinThreads)
    pool
  }

  private def internalConnector(implicit config: Configuration) = {
    val connector = new SocketConnector
    connector.setPort(config("metrics.port").or(8081))
    connector.setName("internal")
    connector
  }

  private def servletContext(implicit servlets: Map[String, ServletHolder],
                             filters: Map[String, FilterHolder]) = {
    val context = new ServletContextHandler()

    for ((pathSpec, servlet) <- servlets) {
      context.addServlet(servlet, pathSpec)
    }

    for ((pathSpec, filter) <- filters) {
      context.addFilter(filter, pathSpec, EnumSet.of(DispatcherType.REQUEST))
    }
    
    context.setConnectorNames(Array("main"))
    context
  }

  private def internalServletContext(implicit tasks: Set[Task]) = {
    val internalContext = new ServletContextHandler()
    internalContext.addServlet(new ServletHolder(new TaskServlet(tasks)), "/tasks/*")
    internalContext.addServlet(new ServletHolder(new MetricsServlet), "/*")
    internalContext.setConnectorNames(Array("internal"))
    internalContext
  }
}
