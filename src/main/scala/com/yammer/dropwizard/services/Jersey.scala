package com.yammer.dropwizard.services

import com.google.inject.servlet.ServletModule
import com.yammer.dropwizard.{Service, ScanningGuiceContainer}

/**
 *
 * @author coda
 */
trait Jersey extends Service {
  def rootUri = "/*"

  override def servlets = super.servlets ++ Seq(new ServletModule {
    override def configureServlets = {
      // TODO: 1/19/11 <coda> -- this needs a wrapper, bad
      serve(rootUri).`with`(classOf[ScanningGuiceContainer])
    }
  })
}
