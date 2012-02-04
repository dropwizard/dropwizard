package com.yammer.dropwizard

import config.Configuration
import bundles.ScalaBundle
import com.codahale.jerkson.ScalaModule

abstract class ScalaService[T <: Configuration](name: String) extends AbstractService[T](name) {
  addBundle(new ScalaBundle(this))
  addJacksonModule(new ScalaModule(Thread.currentThread().getContextClassLoader))
  override final def subclassServiceInsteadOfThis() {}

  final def main(args: Array[String]) {
    run(args)
  }
}
