package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.ConfiguredCommand
import com.yammer.dropwizard.Logging
import com.yammer.dropwizard.Service
import com.beust.jcommander.{Parameter, Parameters}
import com.yammer.dropwizard.config.Bootstrap

@Parameters(commandNames = Array("say"), commandDescription = "Prints out the saying to the console")
class SayCommand extends ConfiguredCommand[ExampleConfiguration] with Logging {
  @Parameter(names = Array("-v", "--verbose"), description = "Yell it a lot")
  private var verbose = false

  protected def run(environment: Bootstrap[ExampleConfiguration],
                    configuration: ExampleConfiguration) {
    for (i <- 1 to (if (verbose) 10 else 1)) {
      log.warn(configuration.saying)
    }
  }
}
