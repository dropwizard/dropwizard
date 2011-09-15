package com.yammer.dropwizard.jetty

import org.eclipse.jetty.server.handler.{GzipHandler => JettyGzipHandler}
import org.eclipse.jetty.server.{Request, Handler}
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.http.HttpHeaders
import javax.servlet.ServletInputStream
import java.util.zip.GZIPInputStream
import java.io.{InputStreamReader, BufferedReader}

class GzipHandler(underlying: Handler) extends JettyGzipHandler {
  setHandler(underlying)

  override def handle(target: String,
                      baseRequest: Request,
                      request: HttpServletRequest,
                      response: HttpServletResponse) {
    super.handle(target,
      baseRequest,
      if (request.getHeader(HttpHeaders.CONTENT_ENCODING) == "gzip") {
        new GzipServletRequest(request, super.setBufferSize())
      } else request,
      response)
  }
}

class GzipServletRequest(req: HttpServletRequest, bufferSize: Int) extends HttpServletRequestWrapper(req) {
  private val input = new GzipServletInputStream(req, bufferSize)
  private val reader = new BufferedReader(new InputStreamReader(input))

  override def getInputStream = input
  override def getReader = reader
}

class GzipServletInputStream(req: HttpServletRequest, bufferSize: Int) extends ServletInputStream {
  private val input = req.getInputStream
  private val decompressed = new GZIPInputStream(input, bufferSize)

  def read() = decompressed.read()

  override def read(b: Array[Byte]) = decompressed.read(b)

  override def read(b: Array[Byte], off: Int, len: Int) = decompressed.read(b,  off, len)

  override def close() {
    decompressed.close()
  }
}
