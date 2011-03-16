package com.yammer.dropwizard.modules

import com.yammer.dropwizard.jersey.ScanningGuiceContainer

case class JerseyServletModule(rootUri: String) extends GuiceServletModule {
  override def configureServlets = {
    serve(rootUri).using[ScanningGuiceContainer]
  }

  override def toString = "%s(%s)".format(getClass.getCanonicalName, rootUri)
}
