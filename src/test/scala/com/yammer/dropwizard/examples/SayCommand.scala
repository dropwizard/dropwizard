package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.{Flag, ConfiguredCommand}
import com.yammer.dropwizard.Service
import com.codahale.fig.Configuration

class SayCommand extends ConfiguredCommand {
  def name = "say"

  override def description = Some("Prints out the saying to console")

  override def options = Flag("v", "verbose", "yell it a lot") :: Nil
  
  def run(service: Service, config: Configuration, opts: Map[String, List[String]], args: List[String]) = {
    val saying = SayingFactory.buildSaying(config)
    for (i <- 1 to (if (opts.contains("verbose")) 10 else 1)) {
      log.warn(saying)
    }
    None
  }
}
