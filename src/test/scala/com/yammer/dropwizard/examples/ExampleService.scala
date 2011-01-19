package com.yammer.dropwizard.examples

import com.yammer.dropwizard.Service
import com.yammer.dropwizard.services.Jersey

/**
 *
 * @author coda
 */
class ExampleService extends Service with Jersey {
  def name = "Example"

  override def modules = Seq(new SayingModule)
}
