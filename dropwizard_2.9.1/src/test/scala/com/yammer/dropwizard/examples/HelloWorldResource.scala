package com.yammer.dropwizard.examples

import javax.ws.rs._
import core.Response.Status
import core.{Response, MediaType}

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
class HelloWorldResource(saying: String) {
  @GET
  def sayHello(@QueryParam("opt") opt: Option[String]) = Seq(saying)

  @POST
  def intentionalError = Response.status(Status.BAD_REQUEST).build()

  @PUT
  def unintentionalError = None.get
}
