package uk.gov.dfid.loader

import Implicits._
import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoConnection
import collection.JavaConversions._
import reactivemongo.bson.{BSONBoolean, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import xml.XML
import java.net.URL
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
import akka.actor.{ActorSystem, Kill, PoisonPill}
import org.neo4j.kernel.EmbeddedGraphDatabase
import akka.pattern.gracefulStop

object Loader extends App {

  // load the configuration options
  val config = ConfigFactory.load

  // create all the necessary connections to data sources etc.
  val mongo     = MongoConnection(config.getStringList("mongodb.servers").toList)
  val sources   = mongo.db(config.getString("mongodb.db")).collection("iati-datasources")
  val validator = new Validator
  val neo4j     = new EmbeddedGraphDatabase(config.getString("neo4j.path"))
  val mapper    = new Mapper(neo4j)

  // first we clear the entire graph db
  neo4j.clearDb

  // iterate over each potential data source type
  ("organisation" :: "activity" :: Nil).map { sourceType =>

    // find all data sources of a particular type.  they must be active
    // to be of relevance to us
    val response = sources.find(BSONDocument(
      "sourceType" -> BSONString(sourceType),
      "active" -> BSONBoolean(true)
    )).toList.map { datasources =>

      // partition by valid status
      datasources.partition { source =>
        // validate the data source
        val url     = source.getAs[BSONString]("url").map(_.value).get
        val ele     = XML.load(url)
        val version = (ele \ "@version").headOption.map(_.text).getOrElse("1.0")
        val stream  = new URL(url).openStream

        validator.validate(stream, version, sourceType)

      } match {
        case (valid, invalid) => {
          // load the valid source
          valid.foreach { validSource  =>
            val uri = validSource.getAs[BSONString]("url").map(_.value).get
            val url = new URL(uri)
            val ele = XML.load(url)

            mapper.map(ele)
          }
        }
      }
    }

    // we will block here for 2 minutes so we can close down everything at the end
    Await.ready(response, 2 minutes)
  }

  println("Shutting Down")
  neo4j.shutdown()
  gracefulStop(mongo.mongosystem, 10 seconds)(ActorSystem("mongodb"))
  println("Shutdown")

  sys.exit
}
