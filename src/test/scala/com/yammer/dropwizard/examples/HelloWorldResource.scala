package com.yammer.dropwizard.examples

import javax.ws.rs.{Produces, GET, Path}
import javax.ws.rs.core.MediaType

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
class HelloWorldResource(implicit saying: String) {
  @GET
  def sayHello = Seq(saying)
}
