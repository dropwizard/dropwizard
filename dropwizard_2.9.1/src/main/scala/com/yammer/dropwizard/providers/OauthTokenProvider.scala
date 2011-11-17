package com.yammer.dropwizard.providers

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.ext.Provider
import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.api.model.Parameter
import com.sun.jersey.spi.inject.InjectableProvider
import com.sun.jersey.core.spi.component.{ComponentScope, ComponentContext}
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable
import com.yammer.dropwizard.BearerToken

@Provider
class OauthTokenProvider extends InjectableProvider[BearerToken, Parameter] {
  def getInjectable(ic: ComponentContext, a: BearerToken, c: Parameter) =
    if (c.getParameterClass.isAssignableFrom(classOf[Option[String]])) {
      new OauthTokenInjectable(a.prefix() + " ")
    } else {
      null
    }

  def getScope = ComponentScope.PerRequest
}

class OauthTokenInjectable(prefix: String) extends AbstractHttpContextInjectable[Option[String]] {
  def getValue(c: HttpContext) = {
    val header = Option(c.getRequest.getHeaderValue(HttpHeaders.AUTHORIZATION))
    header.collect {
      case token if token.startsWith(prefix) => token.substring(prefix.length())
    }
  }
}
