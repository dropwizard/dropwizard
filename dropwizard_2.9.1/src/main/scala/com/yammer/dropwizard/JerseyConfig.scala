package com.yammer.dropwizard

import jersey.LoggingExceptionMapper
import scala.collection.JavaConversions._
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.logula.Logging
import com.sun.jersey.api.core.DefaultResourceConfig
import com.yammer.dropwizard.providers.{OauthTokenProvider}
import com.codahale.jersey.providers.{JerksonProvider}

class JerseyConfig(env: Environment) extends DefaultResourceConfig with Logging {
  (
    Set(
      new LoggingExceptionMapper,
      new JerksonProvider[Any],
      new OauthTokenProvider,
      new ScalaCollectionsQueryParamInjectableProvider
    ) ++ env.resources ++ env.providers
  ).foreach(getSingletons.add)

  setPropertiesAndFeatures(env.jerseyParams)
  getClasses.addAll(env.providerClasses)

  override def validate() {
    env.validate()
  }
}
