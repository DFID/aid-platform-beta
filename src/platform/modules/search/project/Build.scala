import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "search"
  val appVersion      = "1.0-SNAPSHOT"
  
  val extraResolvers = Seq(
    "neo4j"                    at "http://m2.neo4j.org",
    "maven-central"            at "http://repo1.maven.org/maven2",
    "neo4j-public-repository"  at "http://m2.neo4j.org/content/groups/public",
    "yobriefca.se-releases"    at "http://yobriefca.se/repo/maven/releases",
    "yobriefca.se-snapshots"   at "http://yobriefca.se/repo/maven/snapshots"
  )
  
  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "com.yammer.metrics" % "metrics-scala_2.9.1" % "2.1.3",
    "com.typesafe" % "config" % "0.5.2",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.neo4j" % "neo4j-scala" % "0.2.0-M2-SNAPSHOT",
    "org.neo4j" % "neo4j-cypher" % "1.8.M07",
    "com.tristanhunt" %% "knockoff" % "0.8.1",
    "org.elasticsearch" % "elasticsearch" % "0.20.5",
    "com.roundeights" % "hasher" % "0.3" from "http://cloud.github.com/downloads/Nycto/Hasher/hasher_2.9.1-0.3.jar"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= extraResolvers
  )

}
