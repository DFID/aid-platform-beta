package controllers.search

import play.api.mvc._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import uk.gov.dfid.es.ElasticSearch
import util.Properties

object Application extends Controller {

  def search = Action { request =>
    request.getQueryString("query").map { query =>
      if(query.trim.isEmpty) {
        Ok(views.html.search("", 0 , List.empty))
      } else {
        val result = ElasticSearch.search(query, Properties.envOrElse("DFID_ELASTICSEARCH_PATH", "/dfid/elastic" ))
        val (projects, countries) = result.toList.map(_.toMap).partition(_.containsKey("id"))
        val country = countries.maxBy(_("countryBudget").asInstanceOf[Int])

        Ok(views.html.search(query, projects.size , projects :+ country))
      }
    } getOrElse {
      Ok(views.html.search("", 0 , List.empty))
    }

  }
}