import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "cms"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.scalatest" %% "scalatest"   % "1.9.1" % "test",
    "com.tzavellas" %  "sse-guice"   % "0.7.0",
    "org.mindrot"   %  "jbcrypt"     % "0.3m",
    "org.mockito"   %  "mockito-all" % "1.9.5" % "test",

    "org.neo4j" % "neo4j-kernel" % "1.9.M04"
  )

  val validator = SubProject("validator")

  val loader = SubProject("loader").dependsOn(validator)

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  ).dependsOn(
    loader
  ).aggregate(
    loader,
    validator
  )

  /**
   * Creates a sub project reference with the necessary defaults for generating
   * IDEA projects etc.
   * @param name Name of the project
   * @return The defined project
   */
  def SubProject(name: String) = {
    Project(name, file("modules/" + name), settings =
      Defaults.defaultSettings ++ play.Project.intellijCommandSettings("SCALA")
    ).settings(
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "org.specs2" % "specs2_2.10" % "1.13" % "test",
        "org.neo4j" % "neo4j-kernel" % "1.9.M04"
      )
    )
  }
}
