package com.yammer.dropwizard

import collection.JavaConversions._
import lifecycle.Managed
import com.sun.jersey.api.core.ResourceConfig._
import com.codahale.logula.Logging
import com.yammer.metrics.core.HealthCheck
import javax.servlet.{Servlet, Filter}
import com.sun.jersey.core.reflection.MethodList
import javax.ws.rs.{Path, HttpMethod}
import org.eclipse.jetty.servlet.{ServletHolder, FilterHolder}
import org.eclipse.jetty.util.component.LifeCycle
import com.yammer.dropwizard.jetty.NonblockingServletHolder
import com.yammer.dropwizard.tasks.{GarbageCollectionTask, Task}

class Environment extends Logging {
  private[dropwizard] var resources = Set.empty[Object]
  private[dropwizard] var healthChecks = Set.empty[HealthCheck]
  private[dropwizard] var providers = Set.empty[Object]
  private[dropwizard] var managedObjects = IndexedSeq.empty[Managed]
  private[dropwizard] var jettyObjects = IndexedSeq.empty[LifeCycle]
  private[dropwizard] var filters = Map.empty[String, FilterHolder]
  private[dropwizard] var servlets = Map.empty[String, ServletHolder]
  private[dropwizard] var tasks: Set[Task] = Set(new GarbageCollectionTask)
  private[dropwizard] var jerseyParams = Map.empty[String, AnyRef]

  def addResource(resource: Object) {
    if (!isRootResourceClass(resource.getClass)) {
      throw new IllegalArgumentException(resource.getClass.getCanonicalName +
        " is not a @Path-annotated resource class")
    }

    if (annotatedMethods(resource).isEmpty) {
      throw new IllegalArgumentException(resource.getClass.getCanonicalName +
        " has no @GET/@POST/etc-annotated methods")
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

  def manage(jettyObject: LifeCycle) {
    jettyObjects ++= IndexedSeq(jettyObject)
  }

  def addFilter[T <: Filter](filter: T,
                             pathSpec: String,
                             params: Map[String, String] = Map.empty) {
    val holder = new FilterHolder(filter)
    holder.setInitParameters(params)
    filters += pathSpec -> holder
  }

  def addFilterClass[T <: Filter](klass: Class[T],
                                  pathSpec: String,
                                  params: Map[String, String] = Map.empty) {
    val holder = new FilterHolder(klass)
    holder.setInitParameters(params)
    filters += pathSpec -> holder
  }

  def addServlet[T <: Servlet](servlet: T,
                               pathSpec: String,
                               params: Map[String, String] = Map.empty,
                               initOrder: Int = 0) {
    val holder = new NonblockingServletHolder(servlet)
    holder.setInitParameters(params)
    holder.setInitOrder(initOrder)
    servlets += pathSpec -> holder
  }

  def addServletClass[T <: Servlet](klass: Class[T],
                                    pathSpec: String,
                                    params: Map[String, String] = Map.empty,
                                    initOrder: Int = 0) {
    val holder = new ServletHolder(klass)
    holder.setInitParameters(params)
    holder.setInitOrder(initOrder)
    servlets += pathSpec -> holder
  }

  def addTask(task: Task) {
    tasks += task
  }

  def addJerseyParam(name: String, value: AnyRef) {
    jerseyParams += name -> value
  }

  private[dropwizard] def validate() {
    def logResources() {
      def httpMethods(resource: Object) = annotatedMethods(resource).map {
        _.getMetaMethodAnnotations(classOf[HttpMethod]).map { _.value() }
      }.flatten.toIndexedSeq.sorted

      def paths(resource: Object) =
        resource.getClass.getAnnotation(classOf[Path]).value() :: Nil

      val out = new StringBuilder("\n\n")
      for (resource <- resources;
           path <- paths(resource);
           method <- httpMethods(resource)) {
        out.append("    %s %s (%s)\n".format(method, path, resource.getClass.getCanonicalName))
      }
      log.info(out.toString())
    }

    log.debug("resources = %s", resources.mkString("{", ", ", "}"))
    log.debug("providers = %s", providers.mkString("{", ", ", "}"))
    log.debug("health checks = %s", healthChecks.mkString("{", ", ", "}"))
    log.debug("managed objects = %s", managedObjects.mkString("{", ", ", "}"))

    logResources()

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

  private def annotatedMethods(resource: Object) =
    new MethodList(resource.getClass, true).hasMetaAnnotation(classOf[HttpMethod])
}
