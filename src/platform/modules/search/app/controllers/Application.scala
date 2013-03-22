package controllers.search

import play.api._
import play.api.mvc._
import play.libs.Scala._
import scala.collection.JavaConversions._
import scala.collection.immutable.HashSet
import uk.gov.dfid.es.ElasticSearch

object Application extends Controller {

  def search = Action { request =>
  
    def query = request.body.asFormUrlEncoded.get("query")(0)
    val javaResults = ElasticSearch.search(query, scala.util.Properties.envOrElse("DFID_ELASTICSEARCH_PATH", "/dfid/elastic" ))
    val scalaResults = scala.collection.mutable.ListBuffer[Map[String,String]]()
    val scalaList = javaResults.toSet
    scalaList.foreach { map =>
      scalaResults += asScala(map)
    }
      Ok(views.html.search(query, scalaResults.size , scalaResults))

  }
}