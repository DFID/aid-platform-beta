package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dfid.common.models.Country
import play.api.libs.json.{JsValue, Json, Writes}
import play.modules.reactivemongo.MongoController
import uk.gov.dfid.common.api.ReadOnlyApi

class Countries @Inject()(countries: ReadOnlyApi[Country]) extends Controller with MongoController {

  implicit val countryWrites = new Writes[Country] {
    def writes(c: Country): JsValue = {
      val description: String = c.description.getOrElse("")
      Json.obj(
        "code"        -> c.code,
        "name"        -> c.name,
        "description" -> description
      )
    }
  }

  def index = Action {
    Async {
      countries.all.map { all =>
        Ok(Json.toJson(all))
      }
    }
  }

  def view(code: String) = Action {
    Async {
      countries.get(code).map { maybeCountry =>
        maybeCountry.map { country =>
          Ok(Json.toJson(country))
        } getOrElse {
          NotFound(Json.obj(
            "error" -> s"Country with code $code not found."
          ))
        }
      }
    }
  }
}
