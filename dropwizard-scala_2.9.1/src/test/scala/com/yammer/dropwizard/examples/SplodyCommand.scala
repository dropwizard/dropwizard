package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.Command
import com.yammer.dropwizard.Service
import com.beust.jcommander.{Parameter, Parameters}
import com.yammer.dropwizard.config.Bootstrap

@Parameters(commandNames = Array("splody"), commandDescription = "Explodes with an error")
class SplodyCommand extends Command {
  @Parameter(names = Array("-r", "--required"), description = "A required option", required = true)
  private var required = false

  @Parameter(names = Array("-e", "--exception"), description = "Throw an exception")
  private var exception = false

  def run(environment: Bootstrap[_]) {
    if (exception) {
      println("Throwing an exception")
      sys.error("EXPERIENCE BIJ")
    }
  }
}
