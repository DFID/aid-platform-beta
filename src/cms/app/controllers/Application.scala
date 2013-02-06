package controllers

import play.api.mvc._

class Application extends Controller with Secured {

  def index = SecuredAction { user => request =>
    Ok(views.html.index())
  }

}