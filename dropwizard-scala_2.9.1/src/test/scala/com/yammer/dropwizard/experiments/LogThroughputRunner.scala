package com.yammer.dropwizard.experiments

import java.util.concurrent.{TimeUnit, Executors}
import com.yammer.metrics.Instrumented
import com.yammer.metrics.reporting.ConsoleReporter
import org.eclipse.jetty.server._
import org.mockito.Mockito._
import org.eclipse.jetty.http.{HttpFields, HttpURI}
import com.yammer.dropwizard.jetty.AsyncRequestLog

object LogThroughputRunner extends Instrumented {
  val timer = metrics.timer("log", durationUnit = TimeUnit.MICROSECONDS)

  class LogWriter(log: RequestLog) extends Runnable {
    val fields = new HttpFields

    val conn = mock(classOf[AbstractHttpConnection])
    when(conn.getRequestFields).thenReturn(fields)

    val request = new Request(conn)
    request.setRemoteAddr("127.0.0.1")
    request.setUri(new HttpURI("/one/two/three"))
    request.setMethod("GET")
    request.setProtocol("HTTP/1.1")

    val response = new Response(conn)
    response.setStatus(200)

    def run() {
      try {
        for (i <- 1 to 10000) {
          timer.time { log.log(request, response) }
        }
      } catch {
        case e => e.printStackTrace()
      }
    }
  }

  def newLog() = {
//    new NCSARequestLog("./logs/ncsa_request_log_yyyy_mm_dd.log")
    new AsyncRequestLog("./logs/async_request_log_yyyy_mm_dd.log", 1)
  }

  def main(args: Array[String]) {
    ConsoleReporter.enable(1, TimeUnit.SECONDS)
    val log = newLog()
    log.start()
    val pool = Executors.newFixedThreadPool(1000)
    for (i <- 1 to 1000) {
      pool.execute(new LogWriter(log))
    }
    pool.shutdown()
    pool.awaitTermination(10, TimeUnit.MINUTES)
    log.stop()
  }
}
