package com.yammer.dropwizard.modules

import com.yammer.dropwizard.Module
import com.yammer.dropwizard.config.Environment
import com.yammer.dropwizard.providers.OauthTokenProvider
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.jersey.providers.JerksonProvider

object ScalaModule extends Module {
  def initialize(environment: Environment) {
    environment.addProvider(new JerksonProvider[Any])
    environment.addProvider(new OauthTokenProvider)
    environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider)
  }
}
