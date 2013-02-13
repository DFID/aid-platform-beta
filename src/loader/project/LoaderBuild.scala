import sbt._
import sbt.Keys._

object LoaderBuild extends Build {

  lazy val loader = Project("loader", file(".")).settings(
    // basic project settings
    name         := "Loader",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.10.0",

    // Resolvers
    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    // Dependencies
    libraryDependencies ++= Seq(
      // Testing Dependencies
      "org.scalatest"     %% "scalatest"         % "1.9.1" % "test",
      "org.mockito"       %  "mockito-all"       % "1.9.5" % "test",

      // Application Dependencies
      "org.neo4j"         %  "neo4j-kernel"       % "1.8.1",
      "org.neo4j"         %  "neo4j-lucene-index" % "1.8.1",
      "com.typesafe"      %  "config"             % "1.0.0",
      "org.reactivemongo" %% "reactivemongo"      % "0.8"
    )
  )
}
