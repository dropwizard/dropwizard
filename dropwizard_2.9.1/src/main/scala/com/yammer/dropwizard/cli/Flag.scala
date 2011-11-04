package com.yammer.dropwizard.cli

sealed trait CliOption

object Flag {
  def apply(opt: String, name: String): Flag = Flag(opt, Some(name))

  def apply(opt: String, name: String,
            description: String): Flag = Flag(opt, Some(name), Some(description))
}

case class Flag(opt: String,
                name: Option[String] = None,
                description: Option[String] = None,
                hasArg: Boolean = false) extends CliOption

case class FlagGroup(flags: Seq[Flag],
                     required: Boolean = false) extends CliOption
