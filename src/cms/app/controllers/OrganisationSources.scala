package controllers

import play.api.mvc.Controller
import com.google.inject.Inject
import lib.api.{SourceSelector}
import concurrent.ExecutionContext.Implicits.global

class OrganisationSources @Inject()(val sources: SourceSelector) extends Controller with Secured {

  def index = SecuredAction { user => request =>
    Async {
      sources.get("organisation").map { all =>
        Ok(views.html.organisations(all))
      }
    }
  }

  def save = SecuredAction(parse.urlFormEncoded) { user => implicit request =>
    val activated = request.body.get("active").getOrElse(Seq.empty)
    sources.activate("organisation", activated: _*)
    Redirect(routes.Application.index)
  }

  def refresh = SecuredAction { user => request =>
    Async {
      sources.load("organisation").map { _ =>
        Redirect(routes.OrganisationSources.index).flashing(
          "message" -> "OrganisationSource File sources refreshed"
        )
      }
    }
  }
}
