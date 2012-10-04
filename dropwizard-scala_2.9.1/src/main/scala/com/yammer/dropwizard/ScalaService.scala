package com.yammer.dropwizard

import config.{Bootstrap, Configuration}
import bundles.ScalaBundle
import com.fasterxml.jackson.module.scala.DefaultScalaModule

abstract class ScalaService[T <: Configuration] extends Service[T] {
  override def initialize(bootstrap: Bootstrap[T]) {
    bootstrap.addBundle(new ScalaBundle())
    // TODO: 10/3/12 <coda> -- move this to the bundle when bundles can hook into the bootstrap
    bootstrap.getObjectMapperFactory.registerModule(new DefaultScalaModule)
  }

  final def main(args: Array[String]) {
    run(args)
  }
}

