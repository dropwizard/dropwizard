package com.yammer.dropwizard.cli

import java.io.File
import com.codahale.jerkson.ParsingException
import com.yammer.dropwizard.modules.ConfigurationModule
import com.codahale.logula.Logging

trait ConfiguredCommand extends Command with Logging {
  override protected def commandSyntax(jarSyntax: String) =
    "%s %s [options] <config file> [argumemts]".format(jarSyntax, name)

  final def run(opts: Map[String, List[String]], args: List[String]) = args match {
    case filename :: others => {
      val f = new File(filename)
      if (f.exists && f.isFile) {
        try {
          require(new ConfigurationModule(filename))
          runWithConfigFile(opts, others)
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

  def runWithConfigFile(opts: Map[String, List[String]], args: List[String]): Option[String]
}
