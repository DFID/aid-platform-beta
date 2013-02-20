package uk.gov.dfid.common.models

import reactivemongo.bson.{BSONLong, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}

case class CountryStats(
   id          : Option[BSONObjectID],
   code        : String,
   totalBudget : Long
)

object CountryStats {

  implicit object CountryStatsReader extends BSONReader[CountryStats] {
    def fromBSON(doc: BSONDocument): CountryStats = {
      val document = doc.toTraversable

      CountryStats(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("code").map(_.value).get,
        document.getAs[BSONLong]("totalBudget").map(_.value).getOrElse(0)
      )
    }
  }

  implicit object CountryStatsWriter  extends BSONWriter[CountryStats]{
    def toBSON(document: CountryStats): BSONDocument = {
      BSONDocument(
        "_id" -> document.id.getOrElse(BSONObjectID.generate),
        "code" -> BSONString(document.code),
        "totalBudget" -> BSONLong(document.totalBudget)
      )
    }
  }
}