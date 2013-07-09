package controllers.traits.admin

import play.api.mvc.Controller
import models.IatiDataSource
import play.api.templates.Html
import concurrent.ExecutionContext.Implicits.global
import lib.traits.SourceSelector
import controllers.admin.routes

/**
 * Common actions relating to the IatiDataSources controllers
 */
trait IatiDataSourceActions { this : Controller with Secured =>

  val sourceType: String
  val sources: SourceSelector

  def view(sources: List[IatiDataSource]): Html

  def index = SecuredAction { user => request =>
    Async {
      sources.get(sourceType).map { all =>
        Ok(view(all))
      }
    }
  }

  def save = SecuredAction(parse.urlFormEncoded) { user => implicit request =>
    val activated = request.body.get("active").getOrElse(Seq.empty)
    sources.activate(sourceType, activated: _*)
    Redirect(routes.Application.index)
  }

  def refresh = SecuredAction { user => request =>
    Async {
      sources.load(sourceType).map { _ =>
        if(sourceType == "organisation") {
          Redirect(routes.OrganisationSources.index).flashing(
            "message" -> s"${sourceType.capitalize} Sources refreshed",
            "type"    -> "info"
          )
        }
        else {
          Redirect(routes.ActivitySources.index).flashing(
            "message" -> s"${sourceType.capitalize} Sources refreshed",
            "type"    -> "info"
          )
        }
      }
    }
  }
}
