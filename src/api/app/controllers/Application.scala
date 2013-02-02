package controllers

import play.api._
import libs.json.Json
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    render {
      case Accepts.Json => Ok(Json.obj("success" -> true))
      case Accepts.Xml => Ok(
        <response>
          <success>true</success>
        </response>)
    }
  }

}