package uk.gov.dfid.loader

import xml.XML
import Implicits._
import java.net.URL
import com.typesafe.config.{ConfigFactory, Config}

object Loader extends App {

  val config = ConfigFactory.load

  val path = config.getString("neo4j.path")

  val db = new org.neo4j.graphdb.factory.GraphDatabaseFactory()
    .newEmbeddedDatabaseBuilder(path)
    .newGraphDatabase()

  // first we clear the entire graph db
  db.clearDb

  // load the org files then the activity files
  // this could be made simpler but i am not sure about
  // the extra memory overhead
  Seq("organisation", "activity").map { sourceType =>
    sources.get(sourceType, activeOnly = true).map { datasources =>
      datasources.partition { source =>
        val ele = XML.load(source.url)
        val version = (ele \ "@version").headOption.map(_.text).getOrElse("1.0")

        val stream = new URL(source.url).openStream
        validator.validate(stream, version, sourceType)

      } match {
        case (valid, invalid) => {
          logger.debug(s"${valid.size} valid files found")
          logger.debug(s"${invalid.size} invalid files found")

          // log the invalid files
          if (!invalid.isEmpty) {
            val invalidOutput = invalid.map(i => s"${i.title}: ${i.url}").mkString("\n")
            logger.info("Invalid Files")
            logger.info(invalidOutput)
          }

          valid.foreach(s => load(s.url))
          invalid
        }
      }
    }
  }
}
