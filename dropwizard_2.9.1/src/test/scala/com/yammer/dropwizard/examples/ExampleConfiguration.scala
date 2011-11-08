package com.yammer.dropwizard.examples

import reflect.BeanProperty
import com.yammer.dropwizard.config.Configuration

class ExampleConfiguration extends Configuration {
  @BeanProperty
  var saying: String = "Hello, world!"
}
