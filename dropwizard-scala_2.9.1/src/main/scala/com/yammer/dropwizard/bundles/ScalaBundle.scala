package com.yammer.dropwizard.bundles

import com.yammer.dropwizard.Bundle
import com.yammer.dropwizard.config.Environment
import com.yammer.dropwizard.providers.OauthTokenProvider
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.jersey.providers.JerksonProvider

object ScalaBundle extends Bundle {
  def initialize(environment: Environment) {
    environment.addProvider(new JerksonProvider[Any])
    environment.addProvider(new OauthTokenProvider)
    environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider)
  }
}
