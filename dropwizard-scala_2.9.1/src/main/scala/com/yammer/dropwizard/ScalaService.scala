package com.yammer.dropwizard

import config.Configuration

abstract class ScalaService[T <: Configuration] extends Service[T] {
  final def main(args: Array[String]) {
    run(args)
  }
}

