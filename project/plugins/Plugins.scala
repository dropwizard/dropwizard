class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"

  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val mavenSBT = "com.codahale" % "maven-sbt" % "0.1.1"
}
