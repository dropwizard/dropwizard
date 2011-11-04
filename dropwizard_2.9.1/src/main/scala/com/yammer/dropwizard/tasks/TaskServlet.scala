package com.yammer.dropwizard.tasks

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.codahale.logula.Logging

class TaskServlet(tasks: Set[Task]) extends HttpServlet with Logging {
  private val tasksByName = tasks.groupBy { "/" + _.name }.mapValues { _.head }
  
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    tasksByName.get(req.getPathInfo) match {
      case Some(task) => {
        try {
          resp.setContentType("text/plain")
          val output = resp.getWriter
          task.execute(params(req), output)
          output.close()
        } catch {
          case e => {
            log.error(e, "Exception throwing while running %s", task)
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
          }
        }
      }
      case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }

  private def params(req: HttpServletRequest): Map[String, scala.Vector[String]] = {
    var params = Map.empty[String, Vector[String]]
    val names = req.getParameterNames
    while (names.hasMoreElements) {
      val name = names.nextElement().asInstanceOf[String]
      params += name -> (Vector.empty ++ req.getParameterValues(name))
    }
    params
  }
}
