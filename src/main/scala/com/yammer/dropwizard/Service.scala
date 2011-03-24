package com.yammer.dropwizard

import collection.{immutable, mutable}
import com.codahale.logula.Logging
import com.google.inject.Module
import com.yammer.dropwizard.modules.{ServerModule, RequestLogHandlerModule}
import com.yammer.dropwizard.cli.{ServerCommand, Command}
import util.JarAware
import com.yammer.metrics.HealthChecks
import com.yammer.metrics.core.HealthCheck

trait Service extends Logging with JarAware {
  private val modules = new mutable.ArrayBuffer[Module]() ++ Seq(new RequestLogHandlerModule, new ServerModule)

  protected def require(modules: Module*) { this.modules ++= modules }

  private var commands = new immutable.TreeMap[String, Command]
  protected def provide(commands: Command*) { commands.foreach { c => this.commands += c.name -> c } }
  provide(new ServerCommand(this))

  protected def healthCheck(name: String, healthCheck: HealthCheck) {
    HealthChecks.registerHealthCheck(name, healthCheck)
  }

  def name: String

  def banner: Option[String] = None

  private def printUsage(error: Option[String] = None) {
    for (msg <- error) {
      System.err.printf("%s\n\n", msg)
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
          case Some(cmd) => {
            cmd.execute(jarSyntax, modules.toSeq, args)
          }
          case None => printUsage(Some("Unrecognized command: " + command))
        }
      }
    }
  }
}

