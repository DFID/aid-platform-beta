package uk.gov.dfid.common.models

import reactivemongo.bson._
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import org.joda.time.DateTime
import reactivemongo.bson.BSONString

case class AuditLog(id: Option[BSONObjectID], auditType: String, message: String, date: DateTime)

object AuditLog {

  implicit object AuditLogReader extends BSONReader[AuditLog] {
    def fromBSON(doc: BSONDocument): AuditLog = {
      val document = doc.toTraversable

      AuditLog(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("type").map(_.value).get,
        document.getAs[BSONString]("message").map(_.value).get,
        document.getAs[BSONDateTime]("date").map(d => new DateTime(d.value)).get
      )
    }
  }

  implicit object AuditLogWriter extends BSONWriter[AuditLog] {
    def toBSON(document: AuditLog): BSONDocument = {
      BSONDocument(
        "_id"     -> document.id.getOrElse(BSONObjectID.generate),
        "type"    -> BSONString(document.auditType),
        "message" -> BSONString(document.message),
        "date"    -> BSONDateTime(document.date.getMillis)
      )
    }
  }
}