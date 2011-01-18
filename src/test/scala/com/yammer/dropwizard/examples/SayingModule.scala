package com.yammer.dropwizard.examples

import com.yammer.dropwizard.ProviderModule
import com.google.inject.{Singleton, Provides}
import com.codahale.fig.Configuration

class SayingModule extends ProviderModule {
  @Provides
  @Singleton
  def provideSaying(config: Configuration): String = config("saying").asRequired[String]
}
