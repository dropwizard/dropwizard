package com.yammer.dropwizard.scala.params

object LongParam {
  def apply(value: Long): LongParam = LongParam(value.toString)
}

/**
 * Parses longs.
 */
case class LongParam(s: String) extends AbstractParam[Long](s) {
  protected def parse(input: String) = input.toLong

  override protected def renderError(input: String, e: Throwable) =
    "Invalid parameter: %s (Must be an integer value.)".format(input)
}
