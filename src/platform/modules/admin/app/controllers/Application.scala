package controllers.admin

import play.api.mvc._
import lib.traits.{Deployer, Authenticator}
import controllers.traits.admin.Secured
import com.google.inject.Inject
import uk.gov.dfid.loader.DataLoader
import uk.gov.dfid.common.api.ReadOnlyApi
import uk.gov.dfid.common.models.AuditLog
import concurrent.ExecutionContext.Implicits.global

class Application @Inject()(val auth: Authenticator, val deployer: Deployer, val loader: DataLoader, val audits: ReadOnlyApi[AuditLog]) extends Controller with Secured {

  def index = SecuredAction { user => implicit request =>
    Async {
      audits.all.map { logs =>
        Ok(views.html.admin.index(logs))
      }
    }
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