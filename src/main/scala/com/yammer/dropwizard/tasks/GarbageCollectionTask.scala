package com.yammer.dropwizard.tasks

import java.io.PrintWriter

/**
 * Performs a full JVM garbage collection (probably).
 */
class GarbageCollectionTask extends Task("gc") {
  def execute(params: Map[String, Vector[String]], output: PrintWriter) {
    val count = for (countParams <- params.get("runs");
                     c <- countParams.headOption)
                  yield c.toInt

    for (i <- 1 to count.getOrElse(1)) {
      output.println("Running GC...")
      output.flush()
      System.gc()
    }

    output.println("Done!")
  }
}
