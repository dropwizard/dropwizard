package com.yammer.dropwizard.lifecycle

import org.eclipse.jetty.util.component.AbstractLifeCycle

class JettyManager(m: Managed) extends AbstractLifeCycle {
  override def doStop() {
    m.stop()
  }

  override def doStart() {
    m.start()
  }
}
