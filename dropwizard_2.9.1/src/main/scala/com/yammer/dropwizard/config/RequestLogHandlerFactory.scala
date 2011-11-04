package com.yammer.dropwizard.config

import com.codahale.fig.Configuration
import org.eclipse.jetty.server.handler.RequestLogHandler
import com.yammer.dropwizard.jetty.AsyncRequestLog

object RequestLogHandlerFactory {
  def buildHandler(implicit config: Configuration) = {
    val log = new AsyncRequestLog(
      config("request_log.filename").asRequired[String],
      config("request_log.retain_days").asRequired[Int]
    )
    
    val handler = new RequestLogHandler()
    handler.setRequestLog(log)
    handler
  }
}
