package controllers.admin

import play.api.mvc._
import lib.traits.{Deployer, Authenticator}
import controllers.traits.admin.Secured
import com.google.inject.Inject
import uk.gov.dfid.loader.DataLoader
import uk.gov.dfid.common.api.ReadOnlyApi
import uk.gov.dfid.common.models.AuditLog
import concurrent.ExecutionContext.Implicits.global
import org.mindrot.jbcrypt.BCrypt
import play.api.Play

class Application @Inject()(val auth: Authenticator, val deployer: Deployer, val loader: DataLoader, val audits: ReadOnlyApi[AuditLog]) extends Controller with Secured {

  def index = SecuredAction { user => implicit request =>
    Async {
      audits.all.map { logs =>
        Ok(views.html.admin.index(logs))
      }
    }
  }

  def deploy = secured(authroiseDeployment)(() => deployer.deploy)

  def load = secured(authroiseDeployment)(() => loader.load)

  private def secured(authenticate: String => Boolean)(action: () => Unit) = {
    SecuredAction(parse.urlFormEncoded) { user => request =>
      request.body("password").headOption.map(authenticate) match {
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

  private def authroiseDeployment(password: String) = {
    BCrypt.checkpw(password, Play.current.configuration.getString("deployment.password").get)
  }
}