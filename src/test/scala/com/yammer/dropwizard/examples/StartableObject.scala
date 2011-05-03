package com.yammer.dropwizard.examples

import com.codahale.logula.Logging
import com.yammer.dropwizard.lifecycle.Managed

class StartableObject(implicit template: String) extends Managed with Logging {
  override def start() {
    log.info("Starting: %s", template)
  }

  /**
   * N.B.: This actually gets called, but if you're running it through SBT
   * and you hit ^C you won't see the final set of log statements indicating
   * that it actually does the full shutdown. It does. kill -SIGINT the JVM
   * instead of hitting ^C and you'll see the log statements.
   */
  override def stop() {
    log.info("Stopping")
  }
}
