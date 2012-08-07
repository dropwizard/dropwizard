package com.yammer.dropwizard.scala.inject

import javax.ws.rs.QueryParam
import javax.ws.rs.ext.Provider
import com.sun.jersey.api.model.Parameter
import com.sun.jersey.core.spi.component.{ComponentScope, ComponentContext}
import com.sun.jersey.spi.inject.{Injectable, InjectableProvider}
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor

@Provider
class ScalaCollectionsQueryParamInjectableProvider extends InjectableProvider[QueryParam, Parameter] {
  def getScope = ComponentScope.PerRequest

  def getInjectable(ic: ComponentContext, a: QueryParam, c: Parameter): Injectable[_] = {
    val parameterName = c.getSourceName
    if (parameterName != null && !parameterName.isEmpty) {
      buildInjectable(parameterName, c.getDefaultValue, !c.isEncoded, c.getParameterClass)
    } else null
  }

  private def buildExtractor(name: String, default: String, klass: Class[_]): MultivaluedParameterExtractor = {
    if (klass == classOf[Seq[String]]) {
      new ScalaCollectionStringReaderExtractor[Seq](name, default, Seq)
    } else if (klass == classOf[List[String]]) {
      new ScalaCollectionStringReaderExtractor[List](name, default, List)
    } else if (klass == classOf[Vector[String]]) {
      new ScalaCollectionStringReaderExtractor[Vector](name, default, Vector)
    } else if (klass == classOf[IndexedSeq[String]]) {
      new ScalaCollectionStringReaderExtractor[IndexedSeq](name, default, IndexedSeq)
    } else if (klass == classOf[Set[String]]) {
      new ScalaCollectionStringReaderExtractor[Set](name, default, Set)
    } else if (klass == classOf[Option[String]]) {
      new ScalaOptionStringExtractor(name, default)
    } else null
  }

  private def buildInjectable(name: String, default: String, decode: Boolean, klass: Class[_]): Injectable[_ <: Object] = {
    val extractor = buildExtractor(name, default, klass)
    if (extractor != null) {
      new ScalaCollectionQueryParamInjectable(extractor, decode)
    } else null
  }
}
