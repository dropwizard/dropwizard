package com.yammer.dropwizard.util

import java.io.File

trait JarAware {
  def jarSyntax = {
    val location = this.getClass.getProtectionDomain.getCodeSource.getLocation
    val thisJar = new File(location.getFile).getName
    "java -jar %s".format(
      if (thisJar.endsWith(".jar")) {
        thisJar
      } else {
        "project.jar"
      }
    )
  }
}
