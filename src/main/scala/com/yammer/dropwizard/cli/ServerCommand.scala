package com.yammer.dropwizard.cli

import org.eclipse.jetty.server.Server
import com.yammer.dropwizard.Service

class ServerCommand(service: Service) extends ConfiguredCommand {
  def name = "server"

  override protected def commandSyntax(jarSyntax: String) =
    "%s %s <config file>".format(jarSyntax, name)


  override def description = Some("Starts an HTTP server running the service")

  def runWithConfigFile(opts: Map[String, List[String]],
                        args: List[String]) = {
    log.info("Starting %s", service.name)

    service.banner.foreach {s => log.info("\n%s\n", s)}

    val server = injector.getInstance(classOf[Server])
    server.start()
    server.join()
    None
  }
}
