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
import uk.gov.dfid.common.neo4j.SingletonEmbeddedNeo4JDatabaseHasALongName
import sys.process._

trait DataLoader {
  def load: Future[Unit]
}

class Loader @Inject()(database: DefaultDB) extends DataLoader {

  def load = {

    val neo4j      = SingletonEmbeddedNeo4JDatabaseHasALongName.restart(true)
    val sources    = database.collection("iati-datasources")
    val validator  = new Validator
    val mapper     = new Mapper(neo4j)
    val engine     = new ExecutionEngine(neo4j)
    val aggregator = new Aggregator(engine, database)

    // find all data sources of a particular type.  they must be active
    // to be of relevance to us
    val mapTask = sources.find(BSONDocument(
      "active" -> BSONBoolean(true)
    )).toList.map { datasources =>

      // partition by valid status
      datasources.partition { sourceDocument =>

        val source = sourceDocument.toTraversable

        // validate the data source
        val url     = source.getAs[BSONString]("url").map(_.value).get
        val ele     = XML.load(url)
        val version = (ele \ "@version").headOption.map(_.text).getOrElse("1.0")
        val stream  = new URL(url).openStream

        println(s"Validating: $url")

        // validation throws uncontrollable errors
        try{
          validator.validate(stream, version, source.getAs[BSONString]("sourceType").map(_.value).get)
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

    for (
      mappings <- mapTask;
      rollups  <- aggregator.rollupCountryBudgets
    ) yield {
      println("Aggregated Budgets")
    }
  }
}
