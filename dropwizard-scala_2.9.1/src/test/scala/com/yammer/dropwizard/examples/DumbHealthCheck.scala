package com.yammer.dropwizard.examples

import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result

class DumbHealthCheck extends HealthCheck("dumb") {
  def check = Result.healthy
}
