package controllers

import play.api._
import libs.json.Json
import play.api.mvc._

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(Json.obj(
      "links" -> Json.obj(
        "access" -> Json.obj(
          "description" -> "for all queries that return lists, collections or individual records",
          "url" -> routes.Access.index.absoluteURL()
        ),
        "aggregate" -> Json.obj(
          "description" -> "for all aggregations across activities or organisations.",
          "url" -> routes.Aggregate.index.absoluteURL()
        )
      )
    ))
  }

}