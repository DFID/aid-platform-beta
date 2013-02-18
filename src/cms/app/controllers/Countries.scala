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
    Ok(views.html.countries.view(None, Country.form))
  }

  // POST /countries
  def save = Action { implicit request =>
    Country.form.bindFromRequest.fold(
      errors => BadRequest(views.html.countries.view(None, errors)),
      country => Async {
        api.query(BSONDocument("code" -> BSONString(country.code))).map {
          case Nil => {
            api.insert(country)
            Redirect(routes.Countries.index)
          }
          case _ => {
            val errors = Country.form.fill(country).withError("code", "Country code must be unique")
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
          val form = Country.form.fill(country)
          Ok(views.html.countries.view(Some(id), form))
        } getOrElse {
          Redirect(routes.Countries.index)
        }
      }
    }
  }

  // POST /countries/:id
  def update(id: String) = Action { implicit request =>
    Country.form.bindFromRequest.fold(
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
