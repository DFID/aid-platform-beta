package uk.gov.dfid.loader

import Implicits._
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONBoolean, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import xml.XML
import java.net.URL
import concurrent.ExecutionContext.Implicits.global
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.cypher.ExecutionEngine
import com.google.inject.Inject
import concurrent.{Await, Future}
import org.neo4j.graphdb.GraphDatabaseService
import concurrent.duration._

trait DataLoader {
  def load: Future[Unit]
}

class Loader @Inject()(database: DefaultDB, neo4j: GraphDatabaseService) extends DataLoader {

  def load = {

    val sources    = database.collection("iati-datasources")
    val validator  = new Validator
    val mapper     = new Mapper(neo4j)
    val engine     = new ExecutionEngine(neo4j)
    val aggregator = new Aggregator(engine, database)

    // first we clear the entire graph db
    neo4j.clearDb

    // iterate over each potential data source type
    ("organisation" :: "activity" :: Nil).map { sourceType =>

      // find all data sources of a particular type.  they must be active
      // to be of relevance to us
      val mapTask = sources.find(BSONDocument(
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

      // we need to block here util each is done
      Await.ready(mapTask, 2 minutes)
    }

    println("Aggregating Budgets")
    aggregator.rollupCountryBudgets andThen { case _ =>
      println("Aggregated Budgets")
    }

  }
}
