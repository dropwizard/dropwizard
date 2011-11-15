package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.ConfiguredCommand
import org.apache.commons.cli.{Options, CommandLine}
import com.codahale.logula.Logging
import com.yammer.dropwizard.AbstractService

class SayCommand extends ConfiguredCommand[ExampleConfiguration]("say", "Prints out the saying to console") with Logging {
  override def getOptions = {
    val options = new Options
    options.addOption("v", "verbose", false, "yell it a lot")
    options
  }

  protected def run(service: AbstractService[ExampleConfiguration],
                    configuration: ExampleConfiguration,
                    params: CommandLine) {
    for (i <- 1 to (if (params.hasOption("verbose")) 10 else 1)) {
      log.warn(configuration.saying)
    }
  }
}
