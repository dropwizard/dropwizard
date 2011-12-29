package com.yammer.dropwizard.examples

import javax.ws.rs.{WebApplicationException, Produces, GET, Path}
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response.Status
import com.yammer.dropwizard.BearerToken
import com.yammer.metrics.annotation.Timed

@Path("/secret")
@Produces(Array(MediaType.APPLICATION_JSON))
class ProtectedResource {
  @GET
  @Timed
  def access(@BearerToken token: Option[String]) = {
    if (token.isDefined) {
      Map("you" -> ("You are ok, Mr. " + token.get))
    } else throw new WebApplicationException(Status.FORBIDDEN)
  }
}
