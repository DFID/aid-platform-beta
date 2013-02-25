package controllers.admin

import play.api.mvc._
import lib.traits.{Deployer, Authenticator}
import controllers.traits.admin.Secured
import com.google.inject.Inject
import uk.gov.dfid.loader.DataLoader

class Application @Inject()(val auth: Authenticator, val deployer: Deployer, val loader: DataLoader) extends Controller with Secured {

  def index = SecuredAction { user => implicit request =>
    Ok(views.html.admin.index())
  }

  def deploy = secured(() => deployer.deploy)

  def load = secured(() => loader.load)

  private def secured(action: () => Unit) = {
    SecuredAction(parse.urlFormEncoded) { user => request =>
      request.body("password").headOption.map(auth.authenticate(_)) match {
        case Some(true) => {
          action()
          Redirect(routes.Application.index).flashing(
            "message" -> "Action queued",
            "type"    -> "success"
          )
        }
        case _ => Redirect(routes.Application.index).flashing(
          "message" -> "Invalid Password!",
          "type"    -> "error"
        )
      }
    }
  }
}