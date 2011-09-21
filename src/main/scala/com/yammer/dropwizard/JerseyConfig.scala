package com.yammer.dropwizard

import scala.collection.JavaConversions._
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.jersey.providers.{ArrayProvider, JValueProvider, JsonCaseClassProvider}
import com.codahale.logula.Logging
import com.sun.jersey.api.core.DefaultResourceConfig
import com.yammer.dropwizard.providers.{OauthTokenProvider, LoggingExceptionMapper}

class JerseyConfig(env: Environment) extends DefaultResourceConfig with Logging {
  (
    Set(
      new LoggingExceptionMapper,
      new JsonCaseClassProvider,
      new OauthTokenProvider,
      new ScalaCollectionsQueryParamInjectableProvider,
      new JValueProvider,
      new ArrayProvider[Object]
    ) ++ env.resources ++ env.providers
  ).foreach(getSingletons.add)

  setPropertiesAndFeatures(env.jerseyParams)
  getClasses.addAll(env.providerClasses)

  override def validate() {
    env.validate()
  }
}
