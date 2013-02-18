package models

import reactivemongo.bson.{BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import play.api.data._
import play.api.data.Forms._

case class Country(id: Option[BSONObjectID], code: String, name: String)

object Country {

  implicit object CountryReader extends BSONReader[Country]{
    def fromBSON(doc: BSONDocument): Country = {
      val document = doc.toTraversable

      Country(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("code").map(_.value).get,
        document.getAs[BSONString]("name").map(_.value).get
      )
    }
  }

  implicit object CountryWriter extends BSONWriter[Country]{
    def toBSON(country: Country): BSONDocument = {
      BSONDocument(
        "_id" -> country.id.getOrElse(BSONObjectID.generate),
        "code" -> BSONString(country.code),
        "name" -> BSONString(country.name)
      )
    }
  }

  val form = Form(
    mapping(
      "id"   -> ignored[Option[BSONObjectID]](None),
      "code" -> nonEmptyText,
      "name" -> nonEmptyText
    )(Country.apply)
     (Country.unapply)
  )
}