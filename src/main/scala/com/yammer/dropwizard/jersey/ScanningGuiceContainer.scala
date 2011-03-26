package com.yammer.dropwizard.jersey

import com.google.inject.{Inject, Injector, Singleton}
import java.io.File
import com.sun.jersey.api.core.{ResourceConfig, ClasspathResourceConfig}
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import com.sun.jersey.spi.container.servlet.WebConfig

/**
 * A Guice Jersey container which scans the entire classpath for @Path
 * and @Provider annotated classes.
 *
 * @author coda
 */
@Singleton
class ScanningGuiceContainer @Inject() (injector: Injector) extends GuiceContainer(injector) {
  override def getDefaultResourceConfig(props: java.util.Map[String, Object], webConfig: WebConfig) = {
    val config = new ClasspathResourceConfig(classpath)
    config.getFeatures.put(ResourceConfig.FEATURE_DISABLE_WADL, true)
    config
  }

  private def classpath = System.getProperty("java.class.path").split(File.pathSeparator)
}
