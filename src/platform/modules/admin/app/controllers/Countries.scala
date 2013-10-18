package controllers.admin

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import uk.gov.dfid.common.api.Api
import uk.gov.dfid.common.models.Country
import reactivemongo.bson.{BSONString, BSONDocument, BSONObjectID}
import com.google.inject.Inject
import concurrent.ExecutionContext.Implicits.global
import controllers.traits.admin.Secured

class Countries @Inject()(val api: Api[Country]) extends Controller with Secured {

  val form = Form(
    mapping(
      "id"          -> ignored[Option[BSONObjectID]](None),
      "code"        -> nonEmptyText,
      "name"        -> nonEmptyText,
      "description" -> optional(text),
      "population" -> optional(text),
      "lifeExpectancy" -> optional(text),
      "incomeLevel" -> optional(text),
      "belowPovertyLine" -> optional(text),
      "fertilityRate" -> optional(text),
      "gdpGrowthRate" -> optional(text)
    )(Country.apply)
     (Country.unapply)
  )

  // GET /countries
  def index = SecuredAction { user => request =>
    Async {
      api.all.map { countries =>
        Ok(views.html.admin.countries.index(countries))
      }
    }
  }

  // GET /countries/new
  def create = SecuredAction { user => request =>
    Ok(views.html.admin.countries.view(None, form))
  }

  // POST /countries
  def save = SecuredAction { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.countries.view(None, errors)),
      country => Async {
        api.query(BSONDocument("code" -> BSONString(country.code))).map {
          case Nil => {
            api.insert(country)
            Redirect(routes.Countries.index)
          }
          case _ => {
            val errors = form.fill(country).withError("code", "Country code must be unique")
            BadRequest(views.html.admin.countries.view(None, errors))
          }
        }
      }
    )
  }

  // GET /countries/:id/edit
  def edit(id: String) = SecuredAction { user => request =>
    Async {
      api.get(id).map { maybeCountry =>
        maybeCountry.map { country =>
          Ok(views.html.admin.countries.view(Some(id), form.fill(country)))
        } getOrElse {
          Redirect(routes.Countries.index)
        }
      }
    }
  }

  // POST /countries/:id
  def update(id: String) = SecuredAction { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.admin.countries.view(Some(id), errors)),
      country => {
        api.update(id, country)
        Redirect(routes.Countries.index)
      }
    )
  }

  // POST /countries/:id/delete
  def delete(id: String) = SecuredAction { user => request =>
    api.delete(id)
    Redirect(routes.Countries.index)
  }

}
