package com.yammer.dropwizard.scala.inject

import com.sun.jersey.api.ParamException
import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable
import com.sun.jersey.server.impl.model.parameter.multivalued.{MultivaluedParameterExtractor, ExtractorContainerException}

class ScalaCollectionQueryParamInjectable(extractor: MultivaluedParameterExtractor,
                                          decode: Boolean)
  extends AbstractHttpContextInjectable[Object] {

  def getValue(c: HttpContext) = try {
    extractor.extract(c.getUriInfo.getQueryParameters(decode))
  } catch {
    case e: ExtractorContainerException =>
      throw new ParamException.QueryParamException(e.getCause,
        extractor.getName,
        extractor.getDefaultStringValue)
  }
}
