package lib

import play.api.libs.json._
import org.neo4j.graphdb.Node
import play.api.libs.json.JsString
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import collection.JavaConversions._
import uk.gov.dfid.common.models.{CountryStats, Country}

object JsonWriters {

  implicit object nodeWrites extends Writes[Node] {

    def writes(node: Node): JsValue = {
      val properties = node.getPropertyKeys.map { key =>
        (key, node.getProperty(key) match {
          case v: java.lang.String  => JsString(v)
          case v: java.lang.Integer => JsNumber(BigDecimal(v))
          case v: java.lang.Long    => JsNumber(BigDecimal(v))
          case v: java.lang.Double  => JsNumber(BigDecimal(v))
          case v: java.lang.Boolean => JsBoolean(v)
        })
      }

      JsObject(properties.toSeq)
    }
  }

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
}
