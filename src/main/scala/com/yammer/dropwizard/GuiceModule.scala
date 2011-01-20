package com.yammer.dropwizard

import com.google.inject.AbstractModule
import com.google.inject.binder.{LinkedBindingBuilder, ScopedBindingBuilder, AnnotatedBindingBuilder}

/**
 * A Scala-friendly wrapper for common Guice bindings.
 *
 * @author coda
 */
abstract class GuiceModule extends AbstractModule {
  implicit def linkedBindingBuilder2scalaBuilder[A](builder: LinkedBindingBuilder[A]) = new {
    def to[T <: A](implicit mf: Manifest[T]): ScopedBindingBuilder = builder.to(mf.erasure.asInstanceOf[Class[A]])
  }

  protected def bind[A](implicit mf: Manifest[A]): AnnotatedBindingBuilder[A] = bind(mf.erasure.asInstanceOf[Class[A]])
}
