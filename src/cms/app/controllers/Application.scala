package controllers

import play.api._
import play.api.mvc._
import lib.{Authenticator, SimpleAuthenticator}
import com.google.inject.Inject

class Application extends Controller with Secured {

  def index = SecuredAction { user => request =>
    Ok(views.html.index("Your new application is ready."))
  }

}