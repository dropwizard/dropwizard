package com.yammer.dropwizard

import collection.immutable
import util.JarLocation
import com.codahale.logula.Logging
import com.yammer.dropwizard.cli.{ServerCommand, Command}
import com.codahale.fig.Configuration

trait Service extends Logging {
  private val jarLocation = new JarLocation
  private var commands = new immutable.TreeMap[String, Command]
  protected def provide(commands: Command*) { commands.foreach { c => this.commands += c.name -> c } }
  provide(new ServerCommand(this))

  def name: String

  def banner: Option[String] = None

  def configure(implicit config: Configuration, environment: Environment)

  private def printUsage(error: Option[String] = None) {
    for (msg <- error) {
      System.err.printf("%s\n\n", msg)
    }

    printf("%s <command> [arg1 arg2]\n\n", jarLocation)

    println("Commands")
    println("========\n")
    for (cmd <- commands.values) {
      cmd.printUsage(jarLocation.toString)
      println("\n")
    }
  }

  def main(args: Array[String]) {
    args.toList match {
      case Nil | "-h" :: Nil | "--help" :: Nil => printUsage()
      case command :: subArgs => {
        commands.get(command) match {
          case Some(cmd) => {
            cmd.execute(this, jarLocation.toString, subArgs)
          }
          case None => printUsage(Some("Unrecognized command: " + command))
        }
      }
    }
  }
}

