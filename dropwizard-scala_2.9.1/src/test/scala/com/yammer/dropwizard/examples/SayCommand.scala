package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.ConfiguredCommand
import com.yammer.dropwizard.Logging
import com.yammer.dropwizard.config.Bootstrap
import net.sourceforge.argparse4j.inf.{Subparser, Namespace}
import net.sourceforge.argparse4j.impl.Arguments

class SayCommand extends ConfiguredCommand[ExampleConfiguration]("say", "Prints out the saying to the console") with Logging {
  override def configure(subparser: Subparser) {
    super.configure(subparser)
    subparser.addArgument("-v", "--verbose").dest("verbose").action(Arguments.store()).help("Yell it a lot")
  }

  protected def run(bootstrap: Bootstrap[ExampleConfiguration],
                    namespace: Namespace,
                    configuration: ExampleConfiguration) {
    for (i <- 1 to (if (namespace.getBoolean("verbose")) 10 else 1)) {
      logger.warn(configuration.saying)
    }
  }

  protected def run(environment: Bootstrap[ExampleConfiguration],
                    configuration: ExampleConfiguration) {

  }
}
