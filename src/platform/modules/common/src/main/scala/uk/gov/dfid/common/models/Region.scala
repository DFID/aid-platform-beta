package uk.gov.dfid.common.models

import reactivemongo.bson.{BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}

case class Region(
  id: Option[BSONObjectID],
  code: String,
  name: String
)

object Region {

  implicit object RegionReader extends BSONReader[Region]{
    def fromBSON(doc: BSONDocument): Region = {
      val document = doc.toTraversable

      Region(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("code").map(_.value).get,
        document.getAs[BSONString]("name").map(_.value).get
      )
    }
  }

  implicit object RegionWriter extends BSONWriter[Region]{
    def toBSON(document: Region): BSONDocument = {
      BSONDocument(
        "_id"   -> document.id.getOrElse(BSONObjectID.generate),
        "code"  -> BSONString(document.code),
        "name" -> BSONString(document.name)
      )
    }
  }
}
