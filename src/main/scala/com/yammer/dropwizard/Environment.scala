package com.yammer.dropwizard

import lifecycle.Managed
import com.sun.jersey.api.core.ResourceConfig._
import com.codahale.logula.Logging
import com.sun.jersey.api.core.DefaultResourceConfig
import com.yammer.metrics.HealthChecks
import com.yammer.metrics.core.HealthCheck
import providers.LoggingExceptionMapper
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.jersey.providers.{JValueProvider, JsonCaseClassProvider}

class Environment extends DefaultResourceConfig with Logging {
  private[dropwizard] var resources = Set.empty[Object]
  private[dropwizard] var healthChecks = Set.empty[HealthCheck]
  private[dropwizard] var providers = Set[Object](
    new LoggingExceptionMapper,
    new JsonCaseClassProvider,
    new ScalaCollectionsQueryParamInjectableProvider,
    new JValueProvider
  )
  private[dropwizard] var managedObjects = IndexedSeq.empty[Managed]

  def addResource(resource: Object) {
    if (!isRootResourceClass(resource.getClass)) {
      throw new IllegalArgumentException(resource.getClass.getCanonicalName +
        " is not a @Path-annotated resource class")
    }
    resources += resource
    getSingletons.add(resource)
  }

  def addProvider(provider: Object) {
    if (!isProviderClass(provider.getClass)) {
      throw new IllegalArgumentException(provider.getClass.getCanonicalName +
        " is not a @Provider-annotated provider class")
    }
    providers += provider
    getSingletons.add(provider)
  }

  def addHealthCheck(healthCheck: HealthCheck) {
    healthChecks += healthCheck
    HealthChecks.register(healthCheck)
  }

  def manage(managedObject: Managed) {
    managedObjects ++= IndexedSeq(managedObject)
  }

  override def validate() {
    super.validate()

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
!    YOUR USERS DOWN. ADD HEALTHCHECKS OR FEAR THE WRATH OF THE DROP WIZARD.   !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
""")
    }
  }
}
