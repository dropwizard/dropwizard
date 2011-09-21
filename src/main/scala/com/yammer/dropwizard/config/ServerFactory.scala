package com.yammer.dropwizard.config

import scala.collection.JavaConverters._
import java.util.EnumSet
import com.codahale.fig.Configuration
import com.codahale.logula.Logging
import com.yammer.metrics.jetty.InstrumentedHandler
import com.yammer.metrics.reporting.MetricsServlet
import com.yammer.dropwizard.util.QuietErrorHandler
import com.yammer.dropwizard.tasks.{Task, TaskServlet}
import com.yammer.dropwizard.jetty.GzipHandler
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.nio.{BlockingChannelConnector, SelectChannelConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.server.{DispatcherType, Server, Connector}
import org.eclipse.jetty.servlet._
import java.util.concurrent.TimeUnit

object ServerFactory extends Logging {
  def provideServer(implicit config: Configuration,
                    servlets: Map[String, ServletHolder],
                    filters: Map[String, FilterHolder],
                    tasks: Set[Task]) = {
    val server = makeServer(mainConnector, internalConnector)

    val handlers = new HandlerCollection
    handlers.addHandler(servletContext)
    handlers.addHandler(internalServletContext)
    if (config("request_log.enabled").or(false)) {
      handlers.addHandler(RequestLogHandlerFactory.buildHandler)
    }
    server.setHandler(handlers)

    server
  }

  private def newConnector(implicit config: Configuration) =
    config("http.connector").or("blocking_channel") match {
      case "socket" => {
        log.warn("Usage of socket connectors with Jetty 7.4.x-7.5.0 is not recommended.")
        log.warn("Use blocking_channel instead.")
        new SocketConnector
      }
      case "select_channel" => {
        val connector = new SelectChannelConnector
        
        for (value <- config("http.low_resources_connections").asOption[Int]) {
          connector.setLowResourcesConnections(value)
        }

        for (value <- config("http.use_direct_buffers").asOption[Boolean]) {
          connector.setUseDirectBuffers(value)
        }

        connector
      }
      case "blocking_channel" => {
        val connector = new BlockingChannelConnector

        for (value <- config("http.use_direct_buffers").asOption[Boolean]) {
          connector.setUseDirectBuffers(value)
        }

        connector
      }
    }

  private def mainConnector(implicit config: Configuration) = {
    val port = config("http.port").or(8080)
    val connector = newConnector(config)
    config("http.hostname").asOption[String].foreach(connector.setHost)

    for (value <- config("http.acceptor_threads").asOption[Int]) {
      connector.setAcceptors(value)
    }

    for (value <- config("http.forwarded").asOption[Boolean]) {
      connector.setForwarded(value)
    }

    for (value <- config("http.max_idle_time_seconds").asOption[Int]) {
      connector.setMaxIdleTime(TimeUnit.SECONDS.toMillis(value).toInt)
    }

    for (value <- config("http.low_resources_max_idle_time_seconds").asOption[Int]) {
      connector.setLowResourcesMaxIdleTime(TimeUnit.SECONDS.toMillis(value).toInt)
    }

    for (value <- config("http.acceptor_priority_offset").asOption[Int]) {
      connector.setAcceptorPriorityOffset(value)
    }

    for (value <- config("http.accept_queue_size").asOption[Int]) {
      connector.setAcceptQueueSize(value)
    }

    for (value <- config("http.max_buffers").asOption[Int]) {
      connector.setMaxBuffers(value)
    }

    for (value <- config("http.request_buffer_size_kilobytes").asOption[Int]) {
      connector.setRequestBufferSize(value * 1024)
    }

    for (value <- config("http.request_header_buffer_size_kilobytes").asOption[Int]) {
      connector.setRequestHeaderSize(value * 1024)
    }

    for (value <- config("http.response_buffer_size_kilobytes").asOption[Int]) {
      connector.setResponseBufferSize(value * 1024)
    }

    for (value <- config("http.response_header_buffer_size_kilobytes").asOption[Int]) {
      connector.setResponseHeaderSize(value * 1024)
    }

    for (value <- config("http.reuse_address").asOption[Boolean]) {
      connector.setReuseAddress(value)
    }

    for (value <- config("http.so_linger_time_seconds").asOption[Int]) {
      connector.setSoLingerTime(value * 1000)
    }
    
    connector.setPort(port)
    connector.setName("main")
    connector
  }

  private def makeServer(connectors: Connector*)(implicit config: Configuration) = {
    val server = new Server
    connectors.foreach(server.addConnector)
    server.addBean(new QuietErrorHandler)
    for (value <- config("http.enable_date_header").asOption[Boolean]) {
      server.setSendDateHeader(value)
    }
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
    connector.setThreadPool(new QueuedThreadPool(8))
    connector
  }

  private def servletContext(implicit servlets: Map[String, ServletHolder],
                             filters: Map[String, FilterHolder],
                             config: Configuration) = {
    val context = new ServletContextHandler()

    for ((pathSpec, servlet) <- servlets) {
      context.addServlet(servlet, pathSpec)
    }

    for ((pathSpec, filter) <- filters) {
      context.addFilter(filter, pathSpec, EnumSet.of(DispatcherType.REQUEST))
    }
    
    context.setConnectorNames(Array("main"))

    val instrumented = new InstrumentedHandler(context)
    if (config("http.gzip.enabled").or(true)) {
      val gzip = new GzipHandler(instrumented)
      config("http.gzip.min_entity_size_bytes").asOption[Int].foreach(gzip.setMinGzipSize)
      config("http.gzip.buffer_size_kilobytes").asOption[Int].foreach { n => gzip.setBufferSize(n * 1024) }
      config("http.gzip.excluded_user_agents").asOption[Set[String]].foreach { s => gzip.setExcluded(s.asJava) }
      config("http.gzip.mime_types").asOption[Set[String]].foreach {s => gzip.setMimeTypes(s.asJava)}
      gzip
    } else instrumented
  }

  private def internalServletContext(implicit tasks: Set[Task]) = {
    val internalContext = new ServletContextHandler()
    internalContext.addServlet(new ServletHolder(new TaskServlet(tasks)), "/tasks/*")
    internalContext.addServlet(new ServletHolder(new MetricsServlet), "/*")
    internalContext.setConnectorNames(Array("internal"))
    internalContext
  }
}
