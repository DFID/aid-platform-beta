package controllers.search

import play.api._
import play.api.mvc._
import play.libs.Scala._
import uk.gov.dfid.es._
import scala.collection.JavaConversions._
import scala.collection.immutable.HashSet

object Application extends Controller {

  def search(query: String) = Action {
    
    val javaResults = ElasticSearch.search(query, "/dfid/aid-platform-beta/data/elasticsearch")
    val scalaResults = scala.collection.mutable.ListBuffer[Map[String,String]]()
    val scalaList = javaResults.toSet
    scalaList.foreach { map =>
      scalaResults += asScala(map)
    }

    Ok(views.html.search(query, scalaResults))
  }

}