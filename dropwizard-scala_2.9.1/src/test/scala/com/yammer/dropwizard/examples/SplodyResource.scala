package com.yammer.dropwizard.examples

import javax.ws.rs.core.StreamingOutput
import javax.ws.rs.{POST, GET, Path}
import java.io.OutputStream
import com.yammer.metrics.annotation.Timed

@Path("/splode")
class SplodyResource {
  private val dumb: String = null

  /**
   * An error which happens inside a Jersey resource.
   */
  @GET
  @Timed
  def splode() = dumb.toString

  /**
   * An error which happens outside of a Jersey resource.
   */
  @POST
  @Timed
  def sneakySplode(): StreamingOutput = new StreamingOutput {
    def write(output: OutputStream) {
      throw new RuntimeException("OH SWEET GOD WHAT")
    }
  }
}
