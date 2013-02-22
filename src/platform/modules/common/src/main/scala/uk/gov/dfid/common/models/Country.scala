package uk.gov.dfid.common.models

import reactivemongo.bson.{BSONLong, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}

case class Country(
  id:          Option[BSONObjectID],
  code:        String,
  name:        String,
  description: Option[String]
)



object Country {

  implicit object CountryReader extends BSONReader[Country]{
    def fromBSON(doc: BSONDocument): Country = {
      val document = doc.toTraversable

      Country(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("code").map(_.value).get,
        document.getAs[BSONString]("name").map(_.value).get,
        document.getAs[BSONString]("description").map(_.value)
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
        country.description.map("description" -> BSONString(_))
      ).flatten: _*)
    }
  }
}