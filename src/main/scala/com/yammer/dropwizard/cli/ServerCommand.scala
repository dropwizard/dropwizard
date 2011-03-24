package com.yammer.dropwizard.cli

import org.eclipse.jetty.server.Server
import com.yammer.dropwizard.Service
import com.yammer.metrics.core.Metrics

class ServerCommand(service: Service) extends ConfiguredCommand {
  def name = "server"

  override protected def commandSyntax(jarSyntax: String) =
    "%s %s <config file>".format(jarSyntax, name)


  override def description = Some("Starts an HTTP server running the service")

  def runWithConfigFile(opts: Map[String, List[String]],
                        args: List[String]) = {
    if (!Metrics.hasHealthChecks) {
      log.warn("""

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!    THIS SERVICE HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW IF IT    !
!    DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE LETTING     !
!    YOUR USERS DOWN. ADD HEALTHCHECKS OR FEAR THE WRATH OF THE DROP WIZARD.   !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
""")
    }

    log.info("Starting %s", service.name)

    service.banner.foreach {s => log.info("\n%s\n", s)}

    val server = injector.getInstance(classOf[Server])
    server.start()
    server.join()
    None
  }
}
