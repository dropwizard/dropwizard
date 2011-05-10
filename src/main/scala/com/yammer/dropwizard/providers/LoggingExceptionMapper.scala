package com.yammer.dropwizard.providers

import util.Random
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.core.Response.Status
import javax.ws.rs.ext.{Provider, ExceptionMapper}
import com.codahale.logula.Logging

/**
 * Logs unhandled exceptions while returning a unique ID which can be used to
 * track the full stack trace down in the logs.
 *
 * @author coda
 */
@Provider
class LoggingExceptionMapper extends ExceptionMapper[Throwable] with Logging {
  private val rand = new Random

  def toResponse(exception: Throwable) = exception match {
    case e: WebApplicationException => e.getResponse
    case _ => {
      val id = rand.nextLong
      log.error(exception, "Error handling a request, ID:%x", id)
      Response.status(Status.INTERNAL_SERVER_ERROR)
        .`type`(MediaType.TEXT_PLAIN)
        .entity("There was an error processing your request. It has been logged (ID %x).\n".format(id))
        .build
    }
  }
}
