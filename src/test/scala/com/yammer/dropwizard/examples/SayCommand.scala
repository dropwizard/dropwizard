package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.ConfiguredCommand
import org.apache.commons.cli.Options

class SayCommand extends ConfiguredCommand {
  def name = "say"


  override def description = Some("Prints out the saying to console")

  override def cliOptions = {
    val opts = new Options
    opts.addOption("v", "verbose", false, "yell it a lot")
    Some(opts)
  }

  def runWithConfigFile(opts: Map[String, List[String]],
                        args: List[String]) = {
    val saying = injector.getInstance(classOf[String])
    for (i <- 1 to (if (opts.contains("verbose")) 10 else 1)) {
      log.warn(saying)
    }
    None
  }
}
