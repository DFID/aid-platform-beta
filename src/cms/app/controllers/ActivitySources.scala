package controllers

import play.api.mvc.Controller
import com.google.inject.Inject
import lib.api.SourceSelector
import concurrent.ExecutionContext.Implicits.global

class ActivitySources @Inject()(val sources: SourceSelector) extends Controller with Secured {

  def index = SecuredAction { user => request =>
    Async {
      sources.get("activity").map { all =>
        Ok(views.html.activities(all))
      }
    }
  }

  def save = SecuredAction(parse.urlFormEncoded) { user => implicit request =>
    val activated = request.body.get("active").getOrElse(Seq.empty)
    sources.activate("activity", activated: _*)
    Redirect(routes.Application.index)
  }

  def refresh = SecuredAction { user => request =>
    Async {
      sources.load("activity").map { _ =>
        Redirect(routes.ActivitySources.index).flashing(
          "message" -> "Activity File sources refreshed"
        )
      }
    }
  }
}
