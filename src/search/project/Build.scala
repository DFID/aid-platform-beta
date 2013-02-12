import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "search"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.elasticsearch" % "elasticsearch" % "0.20.4",
    "com.tzavellas"     % "sse-guice"     % "0.7.1",
    jdbc,
    anorm
  )



  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
