package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.Command
import com.yammer.dropwizard.config.Bootstrap
import net.sourceforge.argparse4j.inf.{Namespace, Subparser}
import net.sourceforge.argparse4j.impl.Arguments

class SplodyCommand extends Command("splody", "Explodes with an error") {

  def configure(subparser: Subparser) {
    subparser.addArgument("-r", "--required").action(Arguments.storeTrue()).help("A required option").required(true).dest("required")
    subparser.addArgument("-e", "--exception").action(Arguments.storeTrue()).help("Throw an exception").required(true).dest("exception")
  }

  def run(environment: Bootstrap[_], namespace: Namespace) {
    if (namespace.getBoolean("exception")) {
      println("Throwing an exception")
      sys.error("EXPERIENCE BIJ")
    }
  }
}
