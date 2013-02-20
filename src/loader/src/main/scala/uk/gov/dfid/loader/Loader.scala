package uk.gov.dfid.loader

import Implicits._
import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoConnection
import collection.JavaConversions._
import reactivemongo.bson.{BSONBoolean, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import xml.XML
import java.net.URL
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration._
import org.neo4j.kernel.EmbeddedGraphDatabase
import uk.gov.dfid.common.neo4j.Neo4JQueryEngine
import uk.gov.dfid.common.api.CountriesApi

object Loader extends App {

  // load the configuration options
  val config = ConfigFactory.load

  // create all the necessary connections to data sources etc.
  val mongo      = MongoConnection(config.getStringList("mongodb.servers").toList)
  val database   = mongo.db(config.getString("mongodb.db"))
  val sources    = database.collection("iati-datasources")
  val validator  = new Validator
  val neo4j      = new EmbeddedGraphDatabase(config.getString("neo4j.path"))
  val mapper     = new Mapper(neo4j)

  val engine     = new Neo4JQueryEngine(neo4j)
  val aggregator = new Aggregator(engine, new CountriesApi(database), database)

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

        println(s"Validating: $url")

        // validation throws uncontrollable errors
        try{
          validator.validate(stream, version, sourceType)
        } catch {
          case e: Throwable => {
            println(s"Error validating $url - ${e.getMessage}")
            false
          }
        }

      } match {
        case (valid, invalid) => {
          invalid.foreach(println)

          // load the valid source
          valid.foreach { validSource  =>
            val uri = validSource.getAs[BSONString]("url").map(_.value).get
            val url = new URL(uri)
            val ele = XML.load(url)

            println(s"Mapping: $uri")
            mapper.map(ele)
          }
        }
      }
    }

    // we will block here for 2 minutes so we can close down everything at the end
    Await.ready(response, 2 minutes)
  }

  // calculate the country budgets for DFID projects
  // Await.ready(aggregator.rollupCountryBudgets, 2 minutes)

  println(s"Shutting down neo4j")
  neo4j.shutdown()
  println(s"Shutting down mongo")
  reactivemongo.api.MongoConnection.system.shutdown()
  println(s"Shutting down app")
  sys.exit(0)
}
