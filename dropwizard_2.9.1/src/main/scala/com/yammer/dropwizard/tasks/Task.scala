package com.yammer.dropwizard.tasks

import java.io.PrintWriter

abstract class Task(val name: String) {
  def execute(params: Map[String, Vector[String]], output: PrintWriter)
}
