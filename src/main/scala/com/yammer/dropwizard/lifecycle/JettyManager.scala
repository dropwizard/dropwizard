package com.yammer.dropwizard.lifecycle

import collection.JavaConversions._
import org.eclipse.jetty.util.component.{LifeCycle, AbstractLifeCycle}
import com.google.inject.{Key, TypeLiteral, ConfigurationException, Injector}

object JettyManager {
  def collect(injector: Injector): Seq[LifeCycle] = {
    try {
      val manageds = injector.getInstance(Key.get(new TypeLiteral[java.util.Set[Managed]]() {}))
      manageds.map { m => new JettyManager(m) }.toSeq
    } catch {
      case e: ConfigurationException => Nil
    }
  }
}

class JettyManager(m: Managed) extends AbstractLifeCycle {
  override def doStop() = m.stop()

  override def doStart() = m.start()
}
