package com.yammer.dropwizard.cli

import collection.mutable
import com.codahale.logula.Logging
import com.google.inject.{Stage, Guice, Module}
import org.apache.commons.cli.{HelpFormatter, CommandLine, GnuParser, Options}

trait Command extends Logging {
  private val modules = new mutable.ArrayBuffer[Module]

  protected def require(modules: Module*) {this.modules ++= modules}

  def name: String

  def cliOptions: Option[Options] = None

  def execute(modules: Seq[Module], args: Seq[String]): Option[String] = {
    try {
      this.modules ++= modules
      val parser = new GnuParser
      val cmdLine = parser.parse(cliOptions.getOrElse(new Options), args.toArray, true)

      val opts = cmdLine.getOptions.map { o =>
        Option(o.getLongOpt).getOrElse(o.getOpt) ->
            Option(o.getValues).getOrElse(Array.empty[String]).toList
      }.toMap

      run(opts, cmdLine.getArgs.toList)
    } catch {
      case e => Some(e.getMessage)
    }
  }

  def run(opts: Map[String, List[String]], args: List[String]): Option[String]

  protected def commandSyntax = "%s %s [options] [arguments]".format(jarSyntax, name)

  protected lazy val injector = Guice.createInjector(Stage.PRODUCTION, modules: _*)

  def printUsage(error: Option[String] = None) {
    for (msg <- error) {
      System.err.printf("Error: %s\n\n", msg)
    }

    val formatter = new HelpFormatter
    formatter.printHelp(commandSyntax, cliOptions.getOrElse(new Options), false)
  }
}
