package com.yammer.dropwizard.scala.params

object IntParam {
  def apply(value: Int): IntParam = IntParam(value.toString)
}

/**
 * Parses ints.
 */
case class IntParam(s: String) extends AbstractParam[Int](s) {
  protected def parse(input: String) = input.toInt

  override protected def renderError(input: String, e: Throwable) =
    "Invalid parameter: %s (Must be an integer value.)".format(input)
}
