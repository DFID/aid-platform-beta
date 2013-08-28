package controllers

import play.api.libs.json.Json
import play.api.mvc._
import basex._

object Aggregate extends Controller with BaseXSupport {

  def index = Action {
    NotImplemented(Json.obj())
  }

  def projects(code: String) = Action { request =>

    Async {
      withSession { session =>
        session.bind(
          "$country_code" -> code
        )
        Ok(session.run("projects")).as("text/json")
      }
    }
  }
}
