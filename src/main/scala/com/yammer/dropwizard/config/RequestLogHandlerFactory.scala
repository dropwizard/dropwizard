package com.yammer.dropwizard.config

import com.codahale.fig.Configuration
import org.eclipse.jetty.server.NCSARequestLog
import org.eclipse.jetty.server.handler.RequestLogHandler

object RequestLogHandlerFactory {
  def buildHandler(implicit config: Configuration) = {
    val log = new NCSARequestLog
    log.setIgnorePaths(config("request_log.ignore_paths").asList[String].toArray)
    config("request_log.append").asOption[Boolean].foreach(log.setAppend)
    config("request_log.filename").asOption[String].foreach(log.setFilename)
    config("request_log.extended").asOption[Boolean].foreach(log.setExtended)
    config("request_log.include_cookies").asOption[Boolean].foreach(log.setLogCookies)
    config("request_log.include_latency").asOption[Boolean].foreach(log.setLogLatency)
    config("request_log.include_server").asOption[Boolean].foreach(log.setLogServer)
    config("request_log.timezone").asOption[String].foreach(log.setLogTimeZone)
    config("request_log.retain_days").asOption[Int].foreach(log.setRetainDays)

    val handler = new RequestLogHandler()
    handler.setRequestLog(log)
    handler
  }
}
