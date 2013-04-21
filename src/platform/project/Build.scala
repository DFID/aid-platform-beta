import com.typesafe.sbtidea.SbtIdeaPlugin
import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "platform"
  val appVersion      = "1.0-SNAPSHOT"

  resolvers := Seq(
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
  )

  object Dependencies {

    val base = Seq(
      // Testing Dependencies
      "org.specs2"        %% "specs2"        % "1.14"  % "test",
      "org.mockito"       %  "mockito-all"   % "1.9.5" % "test",

      // Application Dependencies
      "com.tzavellas"     %  "sse-guice"     % "0.7.0",
      "org.reactivemongo" %% "reactivemongo" % "0.8",
      "org.mindrot"       %  "jbcrypt"       % "0.3m",
      "joda-time"         %  "joda-time"     % "2.1",
      "org.joda"          %  "joda-convert"  % "1.3",
      "com.typesafe"      %  "config"        % "1.0.0"
    )

    val neo4j = Seq(
      "org.neo4j"    %  "neo4j-kernel"       % "1.9.M04",
      "org.neo4j"    %  "neo4j-lucene-index" % "1.9.M04",
      "org.neo4j"    %  "neo4j-cypher"       % "1.9.M04"
    )

    val elasticSearch = Seq(
      "org.elasticsearch" % "elasticsearch" % "0.20.5",
      "com.spatial4j"     % "spatial4j"     % "0.3"
    )
  }

  lazy val common = Project(
    appName + "-common", file("modules/common"),
    settings = Defaults.defaultSettings ++ SbtIdeaPlugin.ideaSettings
  ).settings(
    organization := "uk.gov.dfid.common",
    scalaVersion := "2.10.0",
    libraryDependencies ++= Dependencies.base
      ++ Dependencies.neo4j
      ++ Dependencies.elasticSearch
  )

  lazy val loader = Project(
    appName + "-loader", file("modules/loader"),
    settings = Defaults.defaultSettings ++ SbtIdeaPlugin.ideaSettings
  ).settings(
    // basic project settings
    name         := "Loader",
    scalaVersion := "2.10.0",
    libraryDependencies ++= Dependencies.base ++ Dependencies.neo4j
  ).dependsOn(common).aggregate(common)

  lazy val search = play.Project(
    appName + "-search", appVersion, Dependencies.base ++ Dependencies.neo4j ++ Dependencies.elasticSearch, path = file("modules/search")
  ).dependsOn(common).aggregate(common)


  lazy val admin = play.Project(
    appName + "-admin", appVersion, Dependencies.base, path = file("modules/admin")
  ).aggregate(
    common, loader
  ).dependsOn(
    common, loader
  )

  val api = play.Project(appName, appVersion, Dependencies.base).settings(
    libraryDependencies ++= Dependencies.neo4j ++ Seq(
      "jp.t2v"       %% "stackable-controller" % "0.2",
      "com.typesafe" %% "play-plugins-mailer"  % "2.1.0"
    )
  ).dependsOn(
    common, admin, search
  ).aggregate(
    common, admin, search
  )

}

