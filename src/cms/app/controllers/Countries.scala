package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.{BSONObjectID, BSONString, BSONDocument}
import uk.gov.dfid.common.traits.Api
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import uk.gov.dfid.common.models.Country

class Countries @Inject()(val api: Api[Country]) extends Controller {

  val form = Form(
    mapping(
      "id"          -> ignored[Option[BSONObjectID]](None),
      "code"        -> nonEmptyText,
      "name"        -> nonEmptyText,
      "description" -> optional(text)
    )(Country.apply)
     (Country.unapply)
  )

  // GET /countries
  def index = Action {
    Async {
      api.all.map { countries =>
        Ok(views.html.countries.index(countries))
      }
    }
  }

  // GET /countries/new
  def create = Action {
    Ok(views.html.countries.view(None, form))
  }

  // POST /countries
  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.countries.view(None, errors)),
      country => Async {
        api.query(BSONDocument("code" -> BSONString(country.code))).map {
          case Nil => {
            api.insert(country)
            Redirect(routes.Countries.index)
          }
          case _ => {
            val errors = form.fill(country).withError("code", "Country code must be unique")
            BadRequest(views.html.countries.view(None, errors))
          }
        }
      }
    )
  }

  // GET /countries/:id/edit
  def edit(id: String) = Action {
    Async {
      api.get(id).map { maybeCountry =>
        maybeCountry.map { country =>
          Ok(views.html.countries.view(Some(id), form.fill(country)))
        } getOrElse {
          Redirect(routes.Countries.index)
        }
      }
    }
  }

  // POST /countries/:id
  def update(id: String) = Action { implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.countries.view(Some(id), errors)),
      country => {
        api.update(id, country)
        Redirect(routes.Countries.index)
      }
    )
  }

  // POST /countries/:id/delete
  def delete(id: String) = Action {
    api.delete(id)
    Redirect(routes.Countries.index)
  }

}
