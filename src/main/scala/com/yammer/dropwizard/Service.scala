package com.yammer.dropwizard

import collection.{immutable, mutable}
import com.codahale.logula.Logging
import com.google.inject.{Stage, Guice, Module}
import com.yammer.dropwizard.modules.{ServerModule, RequestLogHandlerModule}
import com.yammer.dropwizard.cli.{ServerCommand, Command}

trait Service extends Logging with JarAware {
  private val modules = new mutable.ArrayBuffer[Module]() ++ Seq(new RequestLogHandlerModule, new ServerModule)

  protected def require(modules: Module*) { this.modules ++= modules }

  private var commands = new immutable.TreeMap[String, Command]
  protected def provide(commands: Command*) { commands.foreach { c => this.commands += c.name -> c } }
  provide(new ServerCommand(this))

  def name: String

  def banner: Option[String] = None

  protected lazy val injector = Guice.createInjector(Stage.PRODUCTION, modules.toArray: _*)

  private def printUsage(error: Option[String] = None) {
    for (msg <- error) {
      System.err.printf("Error: %s\n\n", msg)
    }

    printf("%s <command> [arg1 arg2]\n\n", jarSyntax)


    println("Commands")
    println("========\n")
    for (cmd <- commands.values) {
      cmd.printUsage(jarSyntax)
      println("\n")
    }
  }

  def main(args: Array[String]) {
    args.toList match {
      case Nil | "-h" :: Nil | "--help" :: Nil => printUsage()
      case command :: args => {
        commands.get(command) match {
          case Some(cmd) => cmd.execute(modules.toSeq, args).foreach {
            s => cmd.printUsage(jarSyntax, Some(s))
          }
          case None => printUsage(Some("Unrecognized command: " + command))
        }
      }
    }
  }
}

