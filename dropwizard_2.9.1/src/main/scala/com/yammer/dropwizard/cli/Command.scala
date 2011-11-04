package com.yammer.dropwizard.cli

import org.apache.commons.cli.{ParseException, GnuParser, Options, HelpFormatter, Option => ApacheOption, OptionGroup}
import com.yammer.dropwizard.Service

trait Command {
  def name: String

  def description: Option[String] = None

  def options: Seq[CliOption] = Nil

  private def cliOptions = {
    def flagToOption(f: Flag) = new ApacheOption(f.opt, f.name.orNull, f.hasArg, f.description.orNull)

    val opts = new Options
    options.foreach {
      case f: Flag => opts.addOption(flagToOption(f))
      case g: FlagGroup => {
        val group = new OptionGroup
        group.setRequired(g.required)
        g.flags.foreach { f => group.addOption(flagToOption(f)) }
        opts.addOptionGroup(group)
      }
    }

    opts
  }

  def execute(service: Service, jarSyntax: String, args: Seq[String]) {
    try {
      if (args == Seq("-h") || args == Seq("--help")) {
        printUsage(jarSyntax, None)
      } else {
        val parser = new GnuParser
        val cmdLine = parser.parse(cliOptions, args.toArray, true)
        val opts = cmdLine.getOptions.map { o =>
          Option(o.getLongOpt).getOrElse(o.getOpt) ->
              Option(o.getValues).getOrElse(Array.empty[String]).toList
        }.toMap

        run(service, opts, cmdLine.getArgs.toList).foreach { e =>
           printUsage(jarSyntax, Some(e))
        }
      }
    } catch {
      case e: ParseException => {
        printUsage(jarSyntax, Some(e.getMessage))
      }
      case e => {
        e.printStackTrace()
      }
    }
  }

  def run(service: Service, opts: Map[String, List[String]], args: List[String]): Option[String]

  protected def commandSyntax(jarSyntax: String) =
    "%s %s [options] [arguments]".format(jarSyntax, name)

  def printUsage(jarSyntax: String, error: Option[String] = None) {
    for (msg <- error) {
      System.err.printf("%s\n\n", msg)
    }

    val formatter = new HelpFormatter
    val title = "%s%s".format(name, description.map { d => ": %s".format(d) }.getOrElse(""))

    println(title)
    println("-" * title.length)
    val opts = cliOptions
    opts.addOption("h", "help", false, "display usage information")
    formatter.printHelp(commandSyntax(jarSyntax), opts, false)
  }
}
