package controllers.admin

import play.api.mvc.Controller
import controllers.traits.admin.Secured
import play.api.data._
import play.api.data.Forms._
import com.google.inject.Inject
import lib.traits.FrontPageManagedContentApi
import concurrent.ExecutionContext.Implicits.global

class FrontPage @Inject()(frontPage: FrontPageManagedContentApi) extends Controller with Secured {

  val form = Form(
    single(
      "whatweachieve" -> list(
        tuple(
          "text" -> text,
          "value" -> text
        )
      )
    )
  )

  def index = SecuredAction { user => request =>
    Async {
      for(
        whatWeAchieve <- frontPage.getWhatWeAchieve
      ) yield {
        Ok(views.html.admin.frontpage(form.fill(whatWeAchieve)))
      }
    }
  }

  def save = SecuredAction { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.frontpage(form)),
      elements => {
        frontPage.saveWhatWeAchieve(elements)
        Redirect(routes.Application.index)
      }
    )
  }
}
