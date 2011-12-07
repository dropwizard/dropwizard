package com.yammer.dropwizard

import config.Configuration
import bundles.ScalaBundle

abstract class ScalaService[T <: Configuration](name: String) extends AbstractService[T](name) {
  addBundle(ScalaBundle)
  override final def subclassServiceInsteadOfThis() {}

  final def main(args: Array[String]) {
    run(args)
  }
}
