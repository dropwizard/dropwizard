package com.yammer.dropwizard

import org.eclipse.jetty.{server => jetty}
import com.codahale.logula.Logging
import com.google.inject.servlet.ServletModule
import com.google.inject.{Stage, Guice, Module}

trait Service extends Logging {
  // TODO: 1/19/11 <coda> -- possible to just expose more Scala-y binding rules?
  def modules: Iterable[Module] = Seq.empty

  def name: String

  def banner: Option[String] = None

  def servlets: Iterable[ServletModule] = Seq.empty

  def run(args: Array[String]) {
    args.toList match {
      case "server" :: filename :: Nil => {
        val includedModules = Seq(
          new ConfigurationModule(filename),
          new ServerModule,
          new RequestLogHandlerModule,
          new ServletModule
        )
        val allModules = (includedModules ++ modules ++ servlets).toArray
        val injector = Guice.createInjector(Stage.PRODUCTION, allModules: _*)
        log.debug("Using modules: %s", allModules.mkString(", "))

        log.info("Starting %s", name)

        if (banner.isDefined) {
          log.info("\n%s\n", banner.get)
        }

        val server = injector.getInstance(classOf[jetty.Server])
        server.start()
        server.join()
      }
      case cmds => {
        System.err.println("Unrecognized command: " + cmds.mkString(" "))

      }
    }
  }
}
