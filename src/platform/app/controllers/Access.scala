package controllers

import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.mvc.Action

class Access extends Controller {

  def index = Action { implicit request =>
    Ok(Json.obj(
      "links" -> Json.obj(
        "organisations" -> Json.obj(
          "description" -> "for returning a list of organisations based on organisational files",
          "url" -> routes.Organisations.index.absoluteURL()
        ),
        "activities" -> Json.obj(
          "description" -> "for returning a list of activities based on DFID data",
          "url" -> routes.Activities.index.absoluteURL()
        )
      )
    ))
  }

}
