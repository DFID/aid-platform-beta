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
    tuple(
      "whatwedo" -> list(
        tuple(
          "text" -> text,
          "value" -> text
        )
      ),
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
        whatWeDo <- frontPage.getWhatWeDo;
        whatWeAchieve <- frontPage.getWhatWeAchieve
      ) yield {
        Ok(views.html.admin.frontpage(
          form.fill(whatWeDo, whatWeAchieve)
        ))
      }
    }
  }

  def save = SecuredAction { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.frontpage(form)),
      elements => {
        val (whatwedo, whatweachieve) = elements
        frontPage.saveWhatWeDo(whatwedo)
        frontPage.saveWhatWeAchieve(whatweachieve)
        Redirect(routes.Application.index)
      }
    )
  }
}
