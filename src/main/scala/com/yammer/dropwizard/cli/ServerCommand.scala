package com.yammer.dropwizard.cli

import com.yammer.metrics.HealthChecks
import com.codahale.fig.Configuration
import com.yammer.dropwizard.config.ServerFactory
import com.sun.jersey.spi.container.servlet.ServletContainer
import com.yammer.metrics.core.DeadlockHealthCheck
import com.yammer.dropwizard.jetty.JettyManaged
import com.yammer.dropwizard.{JerseyConfig, Environment, Service}

class ServerCommand(service: Service) extends ConfiguredCommand {
  def name = "server"

  override protected def commandSyntax(jarSyntax: String) =
    "%s %s <config file>".format(jarSyntax, name)


  override def description = Some("Starts an HTTP server running the service")

  final def run(service: Service, config: Configuration, opts: Map[String, List[String]], args: List[String]) = {
    val env = new Environment
    service.configure(config, env)
    env.healthChecks.foreach(HealthChecks.register)
    HealthChecks.register(new DeadlockHealthCheck)
    env.addServlet(new ServletContainer(new JerseyConfig(env)), "/*")

    val server = ServerFactory.provideServer(config, env.servlets, env.filters)
    env.jettyObjects.foreach(server.addBean)
    env.managedObjects.map { new JettyManaged(_) }.foreach(server.addBean)

    log.info("Starting %s", service.name)
    service.banner.foreach {s => log.info("\n%s\n", s)}

    server.start()
    server.join()

    None
  }
}
