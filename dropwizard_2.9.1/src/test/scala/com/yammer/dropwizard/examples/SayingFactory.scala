package com.yammer.dropwizard.examples

object SayingFactory {
  def buildSaying(implicit config: ExampleConfiguration) = config.saying
}
