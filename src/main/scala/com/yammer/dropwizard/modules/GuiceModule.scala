package com.yammer.dropwizard.modules

import com.google.inject.AbstractModule
import com.google.inject.binder.{LinkedBindingBuilder, ScopedBindingBuilder, AnnotatedBindingBuilder}
import com.google.inject.multibindings.Multibinder

/**
 * A Scala-friendly wrapper for common Guice bindings.
 *
 * @author coda
 */
abstract class GuiceModule extends AbstractModule {
  implicit def linkedBindingBuilder2scalaBuilder[A](builder: LinkedBindingBuilder[A]) = new {
    def to[T <: A](implicit mf: Manifest[T]): ScopedBindingBuilder = builder.to(mf.erasure.asInstanceOf[Class[A]])

    def providedBy[T](implicit mf: Manifest[T]) = "poop"
  }

  protected def bind[A](implicit mf: Manifest[A]): AnnotatedBindingBuilder[A] = bind(mf.erasure.asInstanceOf[Class[A]])

  protected def multibind[A](f: Multibinder[A] => Any)(implicit mf: Manifest[A]) {
    val multi = Multibinder.newSetBinder(binder, mf.erasure.asInstanceOf[Class[A]])
    f(multi)
  }
}
