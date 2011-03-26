package com.yammer.dropwizard.util

trait JarAware {
  def jarSyntax = {
    val thisJar = this.getClass.getProtectionDomain.getCodeSource.getLocation.getFile
    "java -jar %s".format(
      if (thisJar.endsWith(".jar")) {
        thisJar
      } else {
        "project.jar"
      }
    )
  }
}
