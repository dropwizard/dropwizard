import sbt._
import maven._

class DropWizard(info: ProjectInfo) extends DefaultProject(info)
                                            with IdeaProject
                                            with MavenDependencies {
  /**
   * Publish the source as well as the class files.
   */
  override def packageSrcJar = defaultJarPath("-sources.jar")
  val sourceArtifact = Artifact.sources(artifactID)
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

  /**
   * Repositories
   */
  val codasRepo = "Coda's Repo" at "http://repo.codahale.com"
  val googleMaven = "Google Maven" at "http://google-maven-repository.googlecode.com/svn/repository/"
  val sunRepo = "Sun Repo" at "http://download.java.net/maven/2/"

  /**
   * Test Dependencies
   */
  val simplespec = "com.codahale" %% "simplespec" % "0.2.0" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test"

  /**
   * Jersey Dependencies
   */
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % "1.5"
  val jerseyScala = "com.codahale" %% "jersey-scala" % "0.1.2"

  /**
   * Misc Dependencies
   */
  val fig = "com.codahale" %% "fig" % "1.1.1"
  val jerkson = "com.codahale" %% "jerkson" % "0.1.4"
  val jackson = "org.codehaus.jackson" % "jackson-core-asl" % "1.7.1"
  val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA8-SNAPSHOT"

  /**
   * Logging Dependencies
   */
  val slf4jVersion = "1.6.1"
  val slf4jBindings = "org.slf4j" % "slf4j-log4j12" % slf4jVersion
  val jul2slf4j = "org.slf4j" % "jul-to-slf4j" % slf4jVersion
  val logula = "com.codahale" %% "logula" % "2.1.0"

  /**
   * Guice Dependencies
   */
  val guiceServlet = "com.google.inject.extensions" % "guice-servlet" % "2.0"

  /**
   * Jetty Dependencies
   */
  val servletApi = "javax.servlet" % "servlet-api" % "2.5"
  val jettyVersion = "7.2.2.v20101205"
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % jettyVersion
  val jettyServlets = "org.eclipse.jetty" % "jetty-servlets" % jettyVersion

  override def fork = forkRun(List(
    "-server", // make sure we're using the 64-bit server VM
    "-d64",
    "-XX:+UseParNewGC", // use parallel GC for the new generation
    "-XX:+UseConcMarkSweepGC", // use concurrent mark-and-sweep for the old generation
    "-XX:+CMSParallelRemarkEnabled", // use multiple threads for the remark phase
    "-XX:+AggressiveOpts", // use the latest and greatest in JVM tech
    "-XX:+UseFastAccessorMethods", // be sure to inline simple accessor methods
    "-XX:+UseBiasedLocking", // speed up uncontended locks
    "-Xss128k", // reduce the thread stack size, freeing up space for the heap
    "-Xmx500M", // same with the max heap size
    //      "-XX:+PrintGCDetails",                 // log GC details to stdout
    //      "-XX:+PrintGCTimeStamps",
    "-XX:+HeapDumpOnOutOfMemoryError" // dump the heap if we run out of memory
  ))
}
