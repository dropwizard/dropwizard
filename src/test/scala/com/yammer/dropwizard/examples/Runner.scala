package com.yammer.dropwizard.examples

object Runner {
  def main(args: Array[String]) {
    val service = new ExampleService
    service.run(args)
  }
}
