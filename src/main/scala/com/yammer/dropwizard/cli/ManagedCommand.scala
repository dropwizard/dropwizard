package com.yammer.dropwizard.cli

import org.eclipse.jetty.util.component.AggregateLifeCycle
import com.codahale.fig.Configuration
import com.yammer.dropwizard.{Environment, Service}
import com.yammer.dropwizard.lifecycle.JettyManager

trait ManagedCommand extends ConfiguredCommand {
  final def run(service: Service, config: Configuration, opts: Map[String, List[String]], args: List[String]): Option[String] = {
    val aggregate = new AggregateLifeCycle
    val env = new Environment
    service.configure(config, env)
    env.managedObjects.map {new JettyManager(_)}.foreach(aggregate.addBean)
    aggregate.start()
    try {
      run(opts, args)
    } finally {
      aggregate.stop()
    }
  }

  def run(opts: Map[String, List[String]], args: List[String]): Option[String]
}
