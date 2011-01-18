package com.yammer.dropwizard.examples

import com.yammer.dropwizard.Service

/**
 *
 * @author coda
 */
class ExampleService extends Service {
  def name = "Example"

  def modules = Seq(new SayingModule)
}
