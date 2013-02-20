import sbt._
import sbt.Keys._

object CommonBuild extends Build {

  lazy val common = Project("common", file(".")).settings(
    organization := "uk.gov.dfid.common",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.10.0",

    resolvers ++= Seq(
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
    ),

    libraryDependencies ++= Seq(
      // Testing Dependencies
      "org.specs2"        %% "specs2"             % "1.13"  % "test",
      "org.mockito"       %  "mockito-all"        % "1.9.5" % "test",

      // Application Dependencies
      "com.typesafe"      %  "config"             % "1.0.0",
      "org.reactivemongo" %% "reactivemongo"      % "0.8",
      "org.neo4j"         %  "neo4j-kernel"       % "1.8.1",
      "org.neo4j"         %  "neo4j-lucene-index" % "1.8.1",
      "org.neo4j"         %  "neo4j-cypher"       % "1.8.1",
      "com.tzavellas"     %  "sse-guice"          % "0.7.0"
    )
  )
}
