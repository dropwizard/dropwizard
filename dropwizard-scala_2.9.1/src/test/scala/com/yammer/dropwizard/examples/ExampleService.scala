package com.yammer.dropwizard.examples

import com.yammer.dropwizard.config.{Bootstrap, Environment}
import com.yammer.dropwizard.{Logging, ScalaService}

object ExampleService extends ScalaService[ExampleConfiguration] with Logging {
  def initialize(bootstrap: Bootstrap[ExampleConfiguration]) {
    bootstrap.addCommand(new SayCommand)
    bootstrap.addCommand(new SplodyCommand)
  }

  def run(configuration: ExampleConfiguration, environment: Environment) {
    environment.addResource(new HelloWorldResource(configuration.saying))
    environment.addResource(new UploadResource)
    environment.addResource(new SplodyResource)
    environment.addHealthCheck(new DumbHealthCheck)
    environment.manage(new StartableObject(configuration.saying))
  }
}
