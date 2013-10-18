package uk.gov.dfid.common.models

import reactivemongo.bson.{BSONLong, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}

case class Country(
  id:          Option[BSONObjectID],
  code:        String,
  name:        String,
  description: Option[String],
  population: Option[String],
  lifeExpectancy: Option[String],
  incomeLevel: Option[String],
  belowPovertyLine: Option[String],
  fertilityRate: Option[String],
  gdpGrowthRate: Option[String]
)

object Country {

  implicit object CountryReader extends BSONReader[Country]{
    def fromBSON(doc: BSONDocument): Country = {
      val document = doc.toTraversable

      Country(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("code").map(_.value).get,
        document.getAs[BSONString]("name").map(_.value).get,
        document.getAs[BSONString]("description").map(_.value),
        document.getAs[BSONString]("population").map(_.value),
        document.getAs[BSONString]("lifeExpectancy").map(_.value),
        document.getAs[BSONString]("incomeLevel").map(_.value),
        document.getAs[BSONString]("belowPovertyLine").map(_.value),
        document.getAs[BSONString]("fertilityRate").map(_.value),
        document.getAs[BSONString]("gdpGrowthRate").map(_.value)
      )
    }
  }

  implicit object CountryWriter extends BSONWriter[Country]{
    def toBSON(country: Country): BSONDocument = {
      BSONDocument(
        "_id"         -> country.id.getOrElse(BSONObjectID.generate),
        "code"        -> BSONString(country.code),
        "name"        -> BSONString(country.name)
      ).append(Seq(
        country.description.map("description" -> BSONString(_)),
        country.population.map("population" -> BSONString(_)),
        country.lifeExpectancy.map("lifeExpectancy" -> BSONString(_)),
        country.incomeLevel.map("incomeLevel" -> BSONString(_)),
        country.belowPovertyLine.map("belowPovertyLine" -> BSONString(_)),
        country.fertilityRate.map("fertilityRate" -> BSONString(_)),
        country.gdpGrowthRate.map("gdpGrowthRate" -> BSONString(_))
      ).flatten: _*)
    }
  }
}