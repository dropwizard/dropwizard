package com.yammer.dropwizard.modules

import com.google.inject.AbstractModule

abstract class ProviderModule extends AbstractModule {
  def configure = {}

  override def toString = getClass.getCanonicalName
}
