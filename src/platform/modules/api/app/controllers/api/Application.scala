package controllers.api

import play.api._
import libs.json.Json
import play.api.mvc._
import controllers.api

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(Json.obj(
      "links" -> Json.obj(
        "access" -> Json.obj(
          "description" -> "for all queries that return lists, collections or individual records",
          "url" -> api.routes.Access.index.absoluteURL()
        ),
        "aggregate" -> Json.obj(
          "description" -> "for all aggregations across activities or organisations.",
          "url" -> api.routes.Aggregate.index.absoluteURL()
        )
      )
    ))
  }

}