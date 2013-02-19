package controllers

import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.mvc.Action
import com.google.inject.Inject
import org.neo4j.graphdb.GraphDatabaseService

class Access extends Controller {

  def index = Action { implicit request =>
    Ok(Json.obj(
      "links" -> Json.obj(
        "organisations" -> Json.obj(
          "description" -> "for returning a list of organisations based on organisational files",
          "url" -> routes.Organisations.index.absoluteURL()
        ),
        "countries" -> Json.obj(
          "description" -> "for returning a list of countries based on DFID data and Codelists",
          "url" -> routes.Countries.index.absoluteURL()
        )
      )
    ))
  }

}
