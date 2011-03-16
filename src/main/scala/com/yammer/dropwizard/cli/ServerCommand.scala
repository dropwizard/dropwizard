package com.yammer.dropwizard.cli

import org.eclipse.jetty.server.Server
import com.yammer.dropwizard.Service

class ServerCommand(service: Service) extends ConfiguredCommand {
  def name = "server"

  override protected def commandSyntax = "%s %s <config file>".format(jarSyntax, name)

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
