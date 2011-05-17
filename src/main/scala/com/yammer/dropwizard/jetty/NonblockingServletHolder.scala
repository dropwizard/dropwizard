package com.yammer.dropwizard.jetty

import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.server.Request
import javax.servlet.{ServletResponse, ServletRequest, Servlet}

/**
  * A {@link ServletHolder} subclass which removes much of the synchronization
  * around servlet initialization, preferring instead to simply use an
  * already-created servlet instance.
  */
class NonblockingServletHolder(servlet: Servlet) extends ServletHolder(servlet) {
  override def getServlet = servlet

  override def handle(baseRequest: Request, request: ServletRequest, response: ServletResponse) {
    val suspendable = baseRequest.isAsyncSupported
    if (!isAsyncSupported) {
      baseRequest.setAsyncSupported(false)
    }
    try {
      servlet.service(request, response)
    } finally {
      baseRequest.setAsyncSupported(suspendable)
    }
  }
}
