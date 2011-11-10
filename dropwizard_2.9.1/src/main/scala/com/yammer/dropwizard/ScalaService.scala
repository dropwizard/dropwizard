package com.yammer.dropwizard

import config.Configuration
import modules.ScalaModule

abstract class ScalaService[T <: Configuration](name: String) extends AbstractService[T](name) {
  addModule(ScalaModule)
  override final def subclassServiceInsteadOfThis() {}

  final def main(args: Array[String]) {
    run(args)
  }
}
