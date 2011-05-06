package com.yammer.dropwizard.util

import annotation.switch
import java.io.Writer
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.server.handler.ErrorHandler
import org.eclipse.jetty.http.HttpGenerator

class QuietErrorHandler extends ErrorHandler {
  import HttpServletResponse._

  private def errorMessage(request: HttpServletRequest, code: Int) =
    (code: @switch) match {
      case SC_BAD_REQUEST =>
        "Your HTTP client sent a request that this server could not understand."
      case SC_CONFLICT =>
        "The request could not be completed due to a conflict with the " +
          "current state of the resource."
      case SC_EXPECTATION_FAILED =>
        "The server could not meet the expectation given in the Expect " +
          "request header."
      case SC_FORBIDDEN =>
        "You don't have permission to access the requested resource."
      case SC_GONE =>
        "The requested resource used to exist but no longer does."
      case SC_INTERNAL_SERVER_ERROR =>
        "The server encountered an internal error and was unable to complete" +
          " your request."
      case SC_LENGTH_REQUIRED =>
        ("A request with the %s method requires a valid Content-Length" +
          " header.").format(request.getMethod)
      case SC_METHOD_NOT_ALLOWED =>
        ("The %s method is not allowed for the requested " +
          "resouce.").format(request.getMethod)
      case SC_NOT_ACCEPTABLE =>
        "The resource identified by the request is only capable of generating" +
          " response entities which have content characteristics not" +
          " acceptable according to the accept headers sent in the request."
      case SC_NOT_FOUND =>
        "The requested resource could not be found on this server."
      case SC_OK =>
        ""
      case SC_PRECONDITION_FAILED =>
        "The precondition on the request for the resource failed positive" +
          " evaluation."
      case SC_REQUEST_ENTITY_TOO_LARGE =>
        ("The %s method does not allow the data transmitted, or the data" +
          " volume exceeds the capacity limit.").format(request.getMethod)
      case SC_REQUEST_TIMEOUT =>
        "The server closed the network connection because your HTTP client" +
          " didn't finish the request within the specified time."
      case SC_REQUEST_URI_TOO_LONG =>
        "The length of the requested URL exceeds the capacity limit for this" +
          " server. The request cannot be processed."
      case SC_REQUESTED_RANGE_NOT_SATISFIABLE =>
        "The server cannot serve the requested byte range."
      case SC_SERVICE_UNAVAILABLE =>
        "The server is temporarily unable to service your request due to" +
          " maintenance downtime or capacity problems. Please try again later."
      case SC_UNAUTHORIZED =>
        "This server could not verify that you are authorized to access" +
          " this resource.\n" +
          "You either supplied the wrong credentials (e.g., bad password)," +
          " or your HTTP client doesn't understand how to supply the" +
          " required credentials."
      case SC_UNSUPPORTED_MEDIA_TYPE =>
        "The server does not support the media type transmitted in the request."
      case status =>
        "Your request could not be processed: " + HttpGenerator.getReasonBuffer(status)
  }

  override def handleErrorPage(request: HttpServletRequest,
                               writer: Writer,
                               code: Int,
                               message: String) {
    writer.append(errorMessage(request, code)).append("\n\n")
  }
}
