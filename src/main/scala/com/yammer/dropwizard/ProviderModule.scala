package com.yammer.dropwizard

import com.google.inject.AbstractModule

/**
 *
 * @author coda
 */
abstract class ProviderModule extends AbstractModule {
  def configure = {}

  override def toString = getClass.getCanonicalName
}
