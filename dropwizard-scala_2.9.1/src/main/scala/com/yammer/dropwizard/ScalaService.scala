package com.yammer.dropwizard

import config.Configuration
import bundles.ScalaBundle
import com.fasterxml.jackson.module.scala.DefaultScalaModule

abstract class ScalaService[T <: Configuration](name: String) extends Service[T](name) {
  addBundle(new ScalaBundle(this))
  addJacksonModule(new DefaultScalaModule)

  final def main(args: Array[String]) {
    run(args)
  }

  def withBundle(bundle: Bundle) {
    addBundle(bundle)
  }

  def withBundle(bundle: ConfiguredBundle[_ >: T]) {
    addBundle(bundle)
  }
}

