package controllers

import play.api.mvc._
import com.google.inject.Inject
import traits.Secured
import lib.{ConfigurationGenerator, Deployer, Authenticator}
import concurrent.ExecutionContext.Implicits.global

class Application @Inject()(val auth: Authenticator, val deployer: Deployer, val config: ConfigurationGenerator) extends Controller with Secured {

  def index = SecuredAction { user => implicit request =>
    Async {
      config.generate.map { configString =>
        Ok(views.html.index(configString))
      }
    }
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