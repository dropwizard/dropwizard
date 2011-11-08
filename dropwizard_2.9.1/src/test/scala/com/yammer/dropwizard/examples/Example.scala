package com.yammer.dropwizard.examples

import com.yammer.dropwizard.Service
import com.yammer.dropwizard.config.Environment
import com.google.common.base.Optional

object Example extends Service[ExampleConfiguration](classOf[ExampleConfiguration], "example", new SayCommand, new SplodyCommand) {
    override def banner = Optional.of("""
                                                 dP
                                                 88
  .d8888b. dP.  .dP .d8888b. 88d8b.d8b. 88d888b. 88 .d8888b.
  88ooood8  `8bd8'  88'  `88 88'`88'`88 88'  `88 88 88ooood8
  88.  ...  .d88b.  88.  .88 88  88  88 88.  .88 88 88.  ...
  `88888P' dP'  `dP `88888P8 dP  dP  dP 88Y888P' dP `88888P'
                                        88
                                        dP
  """)

  /*

        new JerksonProvider[Any],
      new OauthTokenProvider,
      new ScalaCollectionsQueryParamInjectableProvider

   */

  def configure(configuration: ExampleConfiguration, environment: Environment) {
      implicit val template = SayingFactory.buildSaying
      environment.addResource(new HelloWorldResource)
      environment.addResource(new UploadResource)
      environment.addResource(new ProtectedResource)
      environment.addResource(new SplodyResource)
      environment.addHealthCheck(new DumbHealthCheck)
      environment.manage(new StartableObject)
  }
}
