package com.yammer.dropwizard.examples

import javax.ws.rs.{Produces, GET, Path}
import javax.ws.rs.core.MediaType
import com.google.inject.{Singleton, Inject}

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
@Singleton
class HelloWorldResource @Inject() (saying: String) {
  @GET
  def sayHello = Seq(saying)
}
