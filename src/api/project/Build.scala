import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "api"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.tzavellas" % "sse-guice" % "0.7.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings()
}
