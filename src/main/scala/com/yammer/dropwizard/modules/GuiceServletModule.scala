package com.yammer.dropwizard.modules

import scala.collection.JavaConversions.asJavaMap
import javax.servlet.Filter
import javax.servlet.http.HttpServlet
import com.google.inject.servlet.ServletModule
import com.google.inject.servlet.ServletModule.{ServletKeyBindingBuilder, FilterKeyBindingBuilder}

/**
 * A Scala-friendly wrapper for common Guice servlet bindings.
 *
 * @author coda
 */
class GuiceServletModule extends ServletModule {
  implicit def servletKeyBindingBuilder2scalaBuilder(builder: ServletKeyBindingBuilder) = new {
    def using[A <: HttpServlet](implicit mf: Manifest[A]): Unit =
      builder.`with`(mf.erasure.asInstanceOf[Class[HttpServlet]])

    def using[A <: HttpServlet](initParams: Map[String, String])(implicit mf: Manifest[A]): Unit =
      builder.`with`(mf.erasure.asInstanceOf[Class[HttpServlet]], asJavaMap(initParams))
  }

  implicit def filterKeyBindingBuilder2scalaBuilder(builder: FilterKeyBindingBuilder) = new {
    def through[A <: Filter](implicit mf: Manifest[A]): Unit =
      builder.through(mf.erasure.asInstanceOf[Class[Filter]])

    def through[A <: Filter](initParams: Map[String, String])(implicit mf: Manifest[A]): Unit =
      builder.through(mf.erasure.asInstanceOf[Class[Filter]], asJavaMap(initParams))
  }
}

