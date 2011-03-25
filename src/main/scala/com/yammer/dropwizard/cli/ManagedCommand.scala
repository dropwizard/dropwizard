package com.yammer.dropwizard.cli

import org.eclipse.jetty.util.component.AggregateLifeCycle
import com.yammer.dropwizard.lifecycle.JettyManager

trait ManagedCommand extends Command {
  final def run(opts: Map[String, List[String]], args: List[String]) = {
    val aggregate = new AggregateLifeCycle
    JettyManager.collect(injector).foreach(aggregate.addBean)
    aggregate.start()
    try {
      runWithManagement(opts, args)
    } finally {
      aggregate.stop()
    }
  }

  def runWithManagement(opts: Map[String, List[String]], args: List[String]): Option[String]
}
