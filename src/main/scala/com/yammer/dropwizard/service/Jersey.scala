package com.yammer.dropwizard.service

import com.yammer.dropwizard.modules.JerseyServletModule
import com.yammer.dropwizard.Service

trait Jersey extends Service {
  def rootUri = "/*"

  require(new JerseyServletModule(rootUri))
}
