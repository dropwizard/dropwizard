package com.yammer.dropwizard.services

import com.google.inject.servlet.ServletModule
import com.yammer.dropwizard.{Service, ScanningGuiceContainer}

/**
 *
 * @author coda
 */
trait Jersey extends Service {
  def rootUri = "/*"

  // TODO: 1/19/11 <coda> -- this needs a wrapper, bad
  override def servlets = super.servlets ++ Seq(new JerseyModule(rootUri))
}

case class JerseyModule(rootUri: String) extends ServletModule {
  override def configureServlets = {
    serve(rootUri).`with`(classOf[ScanningGuiceContainer])
  }

  override def toString = "%s(%s)".format(getClass.getCanonicalName, rootUri)
}
