package com.yammer.dropwizard.jetty

import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.LinkedBlockingQueue
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.server.{Authentication, Response, Request, RequestLog}
import org.eclipse.jetty.util.{DateCache, RolloverFileOutputStream}
import java.util.{Locale, ArrayList, TimeZone}
import java.lang.ThreadLocal

object AsyncRequestLog {
  private val i = new AtomicInteger
  private val log = Log.getLogger(classOf[AsyncRequestLog])
}

/**
 * A non-blocking, asynchronous {@link RequestLog} implementation which
 * implements a subset of the functionality of
 * {@link org.eclipse.jetty.server.NCSARequestLog}. Log entries are added to an
 * in-memory queue and an offline thread handles the responsibility of batching
 * them to disk. The date format is fixed, UTC time zone is fixed, and latency
 * is always logged.
 */
class AsyncRequestLog(filenamePattern: Option[String],
                      numberOfFileToRetain: Option[Int]) extends AbstractLifeCycle
                                                                 with RequestLog {
  import AsyncRequestLog._
  import collection.JavaConversions._

  private val queue = new LinkedBlockingQueue[String]
  private val logDateCache = new ThreadLocal[DateCache] {
    override def initialValue() = {
      val cache = new DateCache("dd/MMM/yyyy:HH:mm:ss Z", Locale.getDefault)
      cache.setTimeZoneID("UTC")
      cache
    }
  }
  private val dispatcher = new Dispatcher
  private val dispatchThread = new Thread(dispatcher)
  dispatchThread.setName("async-request-log-dispatcher-" + i.incrementAndGet())
  dispatchThread.setDaemon(true)
  private var writer: PrintWriter = null


  private class Dispatcher extends Runnable {
    @volatile private var stopped = false
    private val statements = new ArrayList[String]

    def run() {
      while (!stopped) {
        try {
          statements.add(queue.take())
          queue.drainTo(statements, 10000)
          statements.foreach(writer.println)
          writer.flush()
          statements.clear()
        } catch {
          case e: InterruptedException => Thread.currentThread().interrupt()
        }
      }
    }

    def stop() {
      stopped = true
    }
  }

  override def doStart() {
    this.writer = new PrintWriter(
      filenamePattern.map { f =>
          val output = new RolloverFileOutputStream(f, true, numberOfFileToRetain.getOrElse(7), TimeZone.getTimeZone("UTC"))
          AsyncRequestLog.log.info("Opened " + output.getDatedFilename)
          output
      }.getOrElse(System.out)
    )
    dispatchThread.start()
  }


  override def doStop() {
    dispatcher.stop()
    writer.close()
  }

  def log(request: Request, response: Response) {
    val buf = new StringBuilder(256)
    buf.append(request.getRemoteAddr).append(" - ")

    val authentication = request.getAuthentication
    if (authentication.isInstanceOf[Authentication.User]) {
      buf.append((authentication.asInstanceOf[Authentication.User]).getUserIdentity.getUserPrincipal.getName)
    } else {
      buf.append(" - ")
    }

    buf.append(" [").append(logDateCache.get().format(request.getTimeStamp)).append("] \"")
    buf.append(request.getMethod).append(' ').append(request.getUri.toString)
    buf.append(' ').append(request.getProtocol).append("\" ")


    if (request.getAsyncContinuation.isInitial) {
      var status = response.getStatus
      if (status <= 0) status = 404
      buf.append(('0' + ((status / 100) % 10)).asInstanceOf[Char])
      buf.append(('0' + ((status / 10) % 10)).asInstanceOf[Char])
      buf.append(('0' + (status % 10)).asInstanceOf[Char])
    } else {
      buf.append("Async")
    }

    val responseLength = response.getContentCount
    if (responseLength >= 0) {
      buf.append(' ')
      if (responseLength > 99999) {
        buf.append(responseLength)
      } else {
        if (responseLength > 9999) buf.append(('0' + ((responseLength / 10000) % 10)).asInstanceOf[Char])
        if (responseLength > 999) buf.append(('0' + ((responseLength / 1000) % 10)).asInstanceOf[Char])
        if (responseLength > 99) buf.append(('0' + ((responseLength / 100) % 10)).asInstanceOf[Char])
        if (responseLength > 9) buf.append(('0' + ((responseLength / 10) % 10)).asInstanceOf[Char])
        buf.append(('0' + (responseLength) % 10).asInstanceOf[Char])
      }
      buf.append(' ')
    } else {
      buf.append(" - ")
    }

    buf.append(' ').append(System.currentTimeMillis - request.getTimeStamp)
    
    queue.add(buf.toString())
  }
}
