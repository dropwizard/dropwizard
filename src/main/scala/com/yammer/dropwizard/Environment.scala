package com.yammer.dropwizard

import lifecycle.Managed
import com.sun.jersey.api.core.ResourceConfig._
import com.codahale.logula.Logging
import com.yammer.metrics.core.HealthCheck
import javax.servlet.{Servlet, Filter}

class Environment extends Logging {
  private[dropwizard] var resources = Set.empty[Object]
  private[dropwizard] var healthChecks = Set.empty[HealthCheck]
  private[dropwizard] var providers = Set.empty[Object]
  private[dropwizard] var managedObjects = IndexedSeq.empty[Managed]
  private[dropwizard] var filters = Map.empty[String, Filter]
  private[dropwizard] var servlets = Map.empty[String, Servlet]

  def addResource(resource: Object) {
    if (!isRootResourceClass(resource.getClass)) {
      throw new IllegalArgumentException(resource.getClass.getCanonicalName +
        " is not a @Path-annotated resource class")
    }
    resources += resource
  }

  def addProvider(provider: Object) {
    if (!isProviderClass(provider.getClass)) {
      throw new IllegalArgumentException(provider.getClass.getCanonicalName +
        " is not a @Provider-annotated provider class")
    }
    providers += provider
  }

  def addHealthCheck(healthCheck: HealthCheck) {
    healthChecks += healthCheck
  }

  def manage(managedObject: Managed) {
    managedObjects ++= IndexedSeq(managedObject)
  }

  def addFilter(filter: Filter, pathSpec: String) {
    filters += pathSpec -> filter
  }

  def addServlet(servlet: Servlet, pathSpec: String) {
    servlets += pathSpec -> servlet
  }

  private[dropwizard] def validate() {
    log.info("resources = %s", resources.mkString("{", ", ", "}"))
    log.info("providers = %s", providers.mkString("{", ", ", "}"))
    log.info("health checks = %s", healthChecks.mkString("{", ", ", "}"))
    log.info("managed objects = %s", managedObjects.mkString("{", ", ", "}"))

    if (healthChecks.isEmpty) {
      log.warn("""

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!    THIS SERVICE HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW IF IT    !
!    DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE LETTING     !
!     YOUR USERS DOWN. YOU SHOULD ADD A HEALTHCHECK FOR EACH DEPENDENCY OF     !
!     YOUR SERVICE WHICH FULLY (BUT LIGHTLY) TESTS YOUR SERVICE'S ABILITY TO   !
!      USE THAT SERVICE. THINK OF IT AS A CONTINUOUS INTEGRATION TEST.         !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
""")
    }
  }
}
