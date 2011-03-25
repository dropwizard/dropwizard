package com.yammer.dropwizard.cli

import collection.JavaConversions._
import org.eclipse.jetty.server.Server
import com.yammer.dropwizard.Service
import com.yammer.metrics.HealthChecks
import com.yammer.metrics.core.HealthCheck
import com.google.inject.{ConfigurationException, Key, TypeLiteral}
import com.yammer.dropwizard.lifecycle.JettyManager

class ServerCommand(service: Service) extends ConfiguredCommand {
  def name = "server"

  override protected def commandSyntax(jarSyntax: String) =
    "%s %s <config file>".format(jarSyntax, name)


  override def description = Some("Starts an HTTP server running the service")

  final def runWithConfigFile(opts: Map[String, List[String]],
                        args: List[String]) = {
    try {
      val healthchecks = injector.getInstance(Key.get(new TypeLiteral[java.util.Set[HealthCheck]]() {}))
      healthchecks.foreach(HealthChecks.registerHealthCheck)
    } catch {
      case e: ConfigurationException => {
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
    }

    log.info("Starting %s", service.name)

    service.banner.foreach {s => log.info("\n%s\n", s)}

    val server = injector.getInstance(classOf[Server])
    JettyManager.collect(injector).foreach(server.addBean)
    server.start()
    server.join()
    None
  }
}
