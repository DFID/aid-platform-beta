import sbt._
import sbt.Keys._
import com.github.retronym.SbtOneJar._

object MigratorBuild extends Build {

  lazy val loader = Project("migrator", file("."), settings = Defaults.defaultSettings ++ oneJarSettings ++ Seq(
    // basic project settings
    name         := "Migrator",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.10.0",

    // Resolvers
    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    // Dependencies
    libraryDependencies ++= Seq(
      // Testing Dependencies
      "org.specs2"        %% "specs2"             % "1.13"  % "test",
      "org.mockito"       %  "mockito-all"        % "1.9.5" % "test",

      // Application Dependencies
      "com.typesafe"      %  "config"             % "1.0.0",
      "org.reactivemongo" %% "reactivemongo"      % "0.8",
      "joda-time"         %  "joda-time"          % "2.1",
      "org.joda"          %  "joda-convert"       % "1.3"
    ),

    artifact in oneJar <<= moduleName(Artifact(_, "dist"))
  ))
}
