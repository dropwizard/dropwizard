package com.yammer.dropwizard.jetty

import org.eclipse.jetty.util.component.AbstractLifeCycle
import com.yammer.dropwizard.lifecycle.Managed

class JettyManaged(m: Managed) extends AbstractLifeCycle {
  override def doStop() {
    m.stop()
  }

  override def doStart() {
    m.start()
  }
}
