package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import lib.traits.Api
import models.Country
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.{BSONString, BSONDocument}

class Countries @Inject()(val api: Api[Country]) extends Controller {

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
    Ok(views.html.countries.create(Country.form))
  }

  // POST /countries
  def save = Action { implicit request =>
    Country.form.bindFromRequest.fold(
      errors => BadRequest(views.html.countries.create(errors)),
      country => Async {
        api.query(BSONDocument("code" -> BSONString(country.code))).map {
          case Nil => {
            api.insert(country)
            Redirect(routes.Application.index)
          }
          case _ => {
            val errors = Country.form.fill(country).withError("code", "Country code must be unique")
            BadRequest(views.html.countries.create(errors))
          }
        }
      }
    )
  }

  // GET /countries/:id/edit
  def edit(id: String) = TODO

  // POST /countries/:id
  def update(id: String) = TODO

  // POST /countries/:id/delete
  def delete(id: String) = TODO

}
