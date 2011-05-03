package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.{Flag, FlagGroup, Command}
import com.yammer.dropwizard.Service

class SplodyCommand extends Command {
  def name = "splody"

  override def description = Some("Explodes with a Guice error")

  override def options =
    FlagGroup(Seq(Flag("r", "required", "a required option")),required = true) ::
    Flag("e", "exception", "throw an exception") ::
    Flag("m", "message", "return an error message") ::
    Nil

  def run(service: Service, opts: Map[String, List[String]], args: List[String]) = {
    if (opts.contains("exception")) {
      println("Throwing an exception")
      error("EXPERIENCE BIJ")
    }

    opts.get("message").map { _ => "Y U NO DO RIGHT THING" }
  }
}
