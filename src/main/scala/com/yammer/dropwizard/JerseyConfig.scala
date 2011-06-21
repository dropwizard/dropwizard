package com.yammer.dropwizard

import com.sun.jersey.api.core.DefaultResourceConfig
import com.codahale.logula.Logging
import providers.LoggingExceptionMapper
import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider
import com.codahale.jersey.providers.{ArrayProvider, JValueProvider, JsonCaseClassProvider}

class JerseyConfig(env: Environment) extends DefaultResourceConfig with Logging {
  (
    Set(
      new LoggingExceptionMapper,
      new JsonCaseClassProvider,
      new ScalaCollectionsQueryParamInjectableProvider,
      new JValueProvider,
      new ArrayProvider[Object]
    ) ++ env.resources ++ env.providers
  ).foreach(getSingletons.add)

  override def validate() {
    env.validate()
  }
}
