
package controllers.search

import play.api.mvc._
import uk.gov.dfid.common.ElasticSearch

object Application extends Controller {

  def search = Action { request =>
    request.getQueryString("query").map { query =>
      if(query.trim.isEmpty) {
        Ok(views.html.search("", 0 , List.empty, List.empty))
      } else {
        val (projects, countries) = ElasticSearch.search(query).partition(_.contains("id"))
        if(countries.isEmpty) {
          Ok(views.html.search(query, projects.size , projects, countries))
        } else {
          val country = countries.maxBy(_("countryBudget").asInstanceOf[Int])
          Ok(views.html.search(query, projects.size , projects, countries))
        }
      }
    } getOrElse {
      Ok(views.html.search("", 0 , List.empty, List.empty))
    }

  }
}