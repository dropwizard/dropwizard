package com.yammer.dropwizard.modules

import com.google.inject.multibindings.Multibinder


class GuiceMultibindingModule[A](interface: Class[A], implementations: Class[_ <: A]*) extends GuiceModule {
  def configure = {
    val multibinder = Multibinder.newSetBinder(binder, interface)
    implementations.foreach { multibinder.addBinding.to(_).asEagerSingleton }
  }
}
