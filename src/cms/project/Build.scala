import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "cms"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Testing Dependencies
    "org.scalatest"     %% "scalatest"     % "1.9.1" % "test",
    "org.mockito"       %  "mockito-all"   % "1.9.5" % "test",

    // Application Dependencies
    "com.tzavellas"     %  "sse-guice"           % "0.7.0",
    "org.mindrot"       %  "jbcrypt"             % "0.3m",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.8",
    "org.reactivemongo" %% "reactivemongo"       % "0.8",

    // WebJar Assets
    "org.webjars"       %  "bootstrap"     % "2.2.2-1",
    "org.webjars"       %  "jquery"        % "1.9.0"
  )

  lazy val main = play.Project(appName, appVersion, appDependencies).dependsOn(common)

  lazy val common = ProjectRef(uri("../common"), "common")
}
