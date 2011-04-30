package com.yammer.dropwizard.config

import com.codahale.fig.Configuration
import java.util.logging.Logger
import org.slf4j.bridge.SLF4JBridgeHandler
import com.codahale.logula.Logging
import org.apache.log4j.Level

object ConfigurationFactory {
  def buildConfiguration(filename: String) = {
    val config = new Configuration(filename)
    configureLogging(config)
    config
  }

  private def configureLogging(config: Configuration) {
    val rootLogger = Logger.getLogger("")
    rootLogger.getHandlers.foreach(rootLogger.removeHandler)
    rootLogger.addHandler(new SLF4JBridgeHandler)
    Logging.configure { log =>
      log.registerWithJMX = true

      log.level = Level.toLevel(config("logging.level").or("info"), Level.INFO)

      for ((name, level) <- config("logging.loggers").asMap[String]) {
        log.loggers(name.toString) = Level.toLevel(level, Level.INFO)
      }

      log.console.enabled = config("logging.console.enabled").or(true)

      config("logging.console.threshold").asOption[String].foreach {l =>
        log.console.threshold = Level.toLevel(l, Level.ALL)
      }

      if (config("logging.file.enabled").or(false)) {
        log.file.enabled = true
        log.file.filename = config("logging.file.filename").asRequired[String]
        log.file.maxSize = config("logging.file.max_log_size_kilobytes").or(10240)
        log.file.retainedFiles = config("logging.file.retain_files").or(1)
        config("logging.file.threshold").asOption[String].foreach {l =>
          log.file.threshold = Level.toLevel(l, Level.ALL)
        }
      }

      if (config("logging.syslog.enabled").or(false)) {
        log.syslog.enabled = true
        log.syslog.host = config("logging.syslog.host").asRequired[String]
        log.syslog.facility = config("logging.syslog.facility").asRequired[String]
      }
    }
  }
}
