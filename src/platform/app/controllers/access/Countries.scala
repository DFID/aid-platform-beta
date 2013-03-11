package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dfid.common.models.{CountryStats, Country}
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.dfid.common.api.ReadOnlyApi
import lib.JsonWriters._
import concurrent.ExecutionContext.Implicits.global

class Countries @Inject()(countries: ReadOnlyApi[Country], stats: ReadOnlyApi[CountryStats]) extends Controller {

  def index = Action {
    Async {
      for(
        c <- countries.all;
        s <- stats.all
      ) yield {
        Ok(Json.toJson(c zip s))
      }
    }
  }

  def view(code: String) = Action {
    Async {
      for(
        c <- countries.get(code);
        s <- stats.get(code)
      ) yield {
        Ok(Json.toJson(c.get -> s.get))
      }
    }
  }
}
