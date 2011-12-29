package com.yammer.dropwizard.examples

import javax.ws.rs._
import core.Response.Status
import core.{Response, MediaType}
import com.yammer.metrics.annotation.Timed

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
class HelloWorldResource(saying: String) {
  @GET
  @Timed
  def sayHello(@QueryParam("opt") opt: Option[String]) = Seq(saying)

  @POST
  @Timed
  def intentionalError = Response.status(Status.BAD_REQUEST).build()

  @PUT
  @Timed
  def unintentionalError = None.get
}
