package com.yammer.dropwizard.examples

import com.codahale.logula.Logging
import com.google.inject.Inject
import com.yammer.dropwizard.lifecycle.Managed

class StartableObject @Inject()(template: String) extends Managed with Logging {
  override def start() {
    log.info("Starting: %s", template)
  }

  override def stop() {
    log.info("Stopping")
  }
}
