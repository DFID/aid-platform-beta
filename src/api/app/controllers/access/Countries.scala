package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dfid.common.models.{CountryStats, Country}
import play.api.libs.json.{JsValue, Json, Writes}
import play.modules.reactivemongo.MongoController
import uk.gov.dfid.common.api.ReadOnlyApi

class Countries @Inject()(countries: ReadOnlyApi[Country], stats: ReadOnlyApi[CountryStats]) extends Controller with MongoController {

  implicit val countryWrites = new Writes[(Country, CountryStats)] {
    def writes(pair: (Country, CountryStats)): JsValue = {
      val (c, s) = pair
      val description: String = c.description.getOrElse("")

      Json.obj(
        "code"        -> c.code,
        "name"        -> c.name,
        "description" -> description,
        "totalBudget" -> s.totalBudget
      )
    }
  }


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
