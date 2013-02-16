package com.yammer.dropwizard

import org.slf4j.{LoggerFactory, Logger}

@deprecated("Logging is being removed in 0.7.0. Use slf4s or grizzled-slf4j.")
trait Logging {
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass)
}
