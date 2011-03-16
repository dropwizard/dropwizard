package com.yammer.dropwizard.examples

import com.codahale.fig.Configuration
import com.google.inject.{Singleton, Provides}
import com.yammer.dropwizard.modules.ProviderModule

class SayingModule extends ProviderModule {
  @Provides
  @Singleton
  def provideSaying(config: Configuration): String = config("saying").asRequired[String]
}
