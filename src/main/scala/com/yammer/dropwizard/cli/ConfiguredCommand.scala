package com.yammer.dropwizard.cli

import java.io.File
import com.codahale.jerkson.ParsingException
import com.codahale.logula.Logging
import com.yammer.dropwizard.config.ConfigurationFactory
import com.codahale.fig.Configuration
import com.yammer.dropwizard.Service

trait ConfiguredCommand extends Command with Logging {
  override protected def commandSyntax(jarSyntax: String) =
    "%s %s [options] <config file> [argumemts]".format(jarSyntax, name)

  final def run(service: Service, opts: Map[String, List[String]], args: List[String]) = args match {
    case filename :: others => {
      val f = new File(filename)
      if (f.exists && f.isFile) {
        try {
          val config = ConfigurationFactory.buildConfiguration(filename)
          run(service, config, opts, others)
        } catch {
          case e: ParsingException => Some("Bad configuration file: " + e.getMessage)
          case e => Some("Error: " + e.getMessage)
        }
      } else {
        Some(filename + " does not exist or is not a file")
      }
    }
    case Nil => Some("no configuration file specified")
  }

  def run(service: Service, config: Configuration, opts: Map[String, List[String]], args: List[String]): Option[String]
}
