package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.Command
import com.yammer.dropwizard.{AbstractService, Service}
import org.apache.commons.cli.{CommandLine, OptionGroup, Options, Option => CliOption}

class SplodyCommand extends Command("splody", "Explodes with an error") {
  override def getOptions = {
    val opts = new Options

    val group = new OptionGroup
    group.setRequired(true)
    group.addOption(new CliOption("r", "required", false, "a required option"))

    opts.addOptionGroup(group)
    opts.addOption("e", "exception", false, "throw an exception")

    opts
  }

  protected def run(service: AbstractService[_], params: CommandLine) {
    if (params.hasOption("exception")) {
      println("Throwing an exception")
      sys.error("EXPERIENCE BIJ")
    }
  }
}
