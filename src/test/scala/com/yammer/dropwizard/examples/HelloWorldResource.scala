package com.yammer.dropwizard.examples

import javax.ws.rs.core.MediaType
import javax.ws.rs.{QueryParam, Produces, GET, Path}

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
class HelloWorldResource(implicit saying: String) {
  @GET
  def sayHello(@QueryParam("opt") opt: Option[String]) = Seq(saying)
}
