package com.yammer.dropwizard.scala.inject

import javax.ws.rs.core.MultivaluedMap
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor

/**
 * Given a parameter name and a possibly-null default value, attempts to extract
 * the first parameter values and return a Some instance, returning the default
 * value if no parameter exists. If defaultValue is null and no parameter
 * exists, returns None.
 */
class ScalaOptionStringExtractor(parameter: String, defaultValue: String)
  extends MultivaluedParameterExtractor {
  private val default = Option(defaultValue)

  def getName = parameter

  def getDefaultStringValue = defaultValue

  def extract(parameters: MultivaluedMap[String, String]) =
    Option(parameters.getFirst(parameter)).orElse(default)
}
