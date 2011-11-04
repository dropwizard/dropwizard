package com.yammer.dropwizard.examples

import com.codahale.fig.Configuration

object SayingFactory {
  def buildSaying(implicit config: Configuration) = config("saying").asRequired[String]
}
