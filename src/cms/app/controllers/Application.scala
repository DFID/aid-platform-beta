package controllers

import play.api.mvc._
import com.google.inject.Inject
import traits.Secured
import lib.{Deployer, Authenticator}

class Application @Inject()(val auth: Authenticator, val deployer: Deployer) extends Controller with Secured {

  def index = SecuredAction { user => implicit request =>
    Ok(views.html.index())
  }

  def deploy = SecuredAction(parse.urlFormEncoded) { user => request =>
    request.body("password").headOption.map(auth.authenticate(_)) match {
      case Some(true) => {
        deployer.deploy
        Redirect(routes.Application.index).flashing(
          "message" -> "Deployment queued",
          "type"    -> "success"
        )
      }
      case _ => Redirect(routes.Application.index).flashing(
        "message" -> "Cannot publish.  Invalid Password!",
        "type"    -> "error"
      )
    }
  }

}