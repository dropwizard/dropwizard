package com.yammer.dropwizard

import logging.Log

trait Logging {
  protected lazy val log: Log = Log.forClass(getClass)
}
