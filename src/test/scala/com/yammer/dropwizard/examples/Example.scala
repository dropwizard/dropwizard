package com.yammer.dropwizard.examples

import com.codahale.fig.Configuration
import com.yammer.dropwizard.{Environment, Service}

object Example extends Service {
  def name = "Example"

  override def banner = Some("""
                                               dP
                                               88
.d8888b. dP.  .dP .d8888b. 88d8b.d8b. 88d888b. 88 .d8888b.
88ooood8  `8bd8'  88'  `88 88'`88'`88 88'  `88 88 88ooood8
88.  ...  .d88b.  88.  .88 88  88  88 88.  .88 88 88.  ...
`88888P' dP'  `dP `88888P8 dP  dP  dP 88Y888P' dP `88888P'
                                      88
                                      dP
""")
  
  provide(new SayCommand, new SplodyCommand)

  def configure(config: Configuration, environment: Environment) {
    implicit val template = SayingFactory.buildSaying(config)
    environment.addResource(new HelloWorldResource)
    environment.addHealthCheck(new DumbHealthCheck)
    environment.manage(new StartableObject)
  }
}
