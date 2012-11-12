package com.yammer.dropwizard.examples

import com.yammer.dropwizard.config.Configuration
import com.fasterxml.jackson.annotation.JsonProperty

class ExampleConfiguration extends Configuration {
  @JsonProperty
  var saying: String = "Hello, world!"
}
