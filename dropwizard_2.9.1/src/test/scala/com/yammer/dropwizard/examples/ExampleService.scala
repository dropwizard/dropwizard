package com.yammer.dropwizard.examples

import com.yammer.dropwizard.config.Environment
import com.yammer.dropwizard.ScalaService

object ExampleService extends ScalaService[ExampleConfiguration]("example") {
  addCommand(new SayCommand)
  addCommand(new SplodyCommand)
  setBanner("""
                                                 dP
                                                 88
  .d8888b. dP.  .dP .d8888b. 88d8b.d8b. 88d888b. 88 .d8888b.
  88ooood8  `8bd8'  88'  `88 88'`88'`88 88'  `88 88 88ooood8
  88.  ...  .d88b.  88.  .88 88  88  88 88.  .88 88 88.  ...
  `88888P' dP'  `dP `88888P8 dP  dP  dP 88Y888P' dP `88888P'
                                        88
                                        dP
  """)

  def initialize(configuration: ExampleConfiguration, environment: Environment) {
    environment.addResource(new HelloWorldResource(configuration.saying))
    environment.addResource(new UploadResource)
    environment.addResource(new ProtectedResource)
    environment.addResource(new SplodyResource)
    environment.addHealthCheck(new DumbHealthCheck)
    environment.manage(new StartableObject(configuration.saying))
  }
}
