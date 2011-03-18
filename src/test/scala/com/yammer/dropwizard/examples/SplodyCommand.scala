package com.yammer.dropwizard.examples

import com.yammer.dropwizard.cli.{Flag, FlagGroup, Command}

class SplodyCommand extends Command {
  def name = "splody"

  override def description = Some("Explodes with a Guice error")

  override def options =
    FlagGroup(Seq(Flag("r", "required", "a required option")),required = true) ::
    Flag("g", "guice", "do something dumb with Guice") ::
    Flag("e", "exception", "throw an exception") ::
    Flag("m", "message", "return an error message") ::
    Nil

  def run(opts: Map[String, List[String]], args: List[String]) = {
    if (opts.contains("guice")) {
      println("Using the injector to get an instance of something Guice doesn't know about")
      injector.getInstance(classOf[Command])
    }

    if (opts.contains("exception")) {
      println("Throwing an exception")
      error("EXPERIENCE BIJ")
    }
    
    opts.get("message").map { _ => "Y U NO DO RIGHT THING" }
  }
}
