package com.yammer.dropwizard.services

import com.yammer.dropwizard.{GuiceServletModule, Service, ScanningGuiceContainer}

/**
 *
 * @author coda
 */
trait Jersey extends Service {
  def rootUri = "/*"

  // TODO: 1/19/11 <coda> -- this needs a wrapper, bad
  override def servlets = super.servlets ++ Seq(new JerseyModule(rootUri))
}

case class JerseyModule(rootUri: String) extends GuiceServletModule {
  override def configureServlets = {
    serve(rootUri).using[ScanningGuiceContainer]
  }

  override def toString = "%s(%s)".format(getClass.getCanonicalName, rootUri)
}
