package com.yammer.dropwizard

import org.eclipse.jetty.{server => jetty}
import com.google.inject.{Stage, Guice, Module}
import com.codahale.logula.Logging

/**
 *
 *
 * @author coda
 */
abstract class Service extends Logging {
  def modules: Iterable[Module]

  def name: String

  def run(filename: String) {
    val includedModules = Seq(new ConfigurationModule(filename),
                              new ServerModule,
                              new RequestLogHandlerModule)
    val allModules = (includedModules ++ modules).toArray
    val injector = Guice.createInjector(Stage.PRODUCTION, allModules:_*)
    log.debug("Using modules: %s", allModules.mkString(", "))

    log.info("Starting %s", name)
    val server = injector.getInstance(classOf[jetty.Server])
    server.start()
    server.join()
  }
}
