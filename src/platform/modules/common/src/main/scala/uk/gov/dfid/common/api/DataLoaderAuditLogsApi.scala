package uk.gov.dfid.common.api

import uk.gov.dfid.common.models.AuditLog
import reactivemongo.api.{DefaultDB, SortOrder, QueryBuilder}
import com.google.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import org.joda.time.DateTime

class ReadOnlyDataLoaderAuditLogsApi @Inject()(database: DefaultDB)  extends ReadOnlyApi[AuditLog]{

  lazy val logs = database.collection("dataload-audits")

  implicit val reader = AuditLog.AuditLogReader

  def all = {
    val yesterday = DateTime.now.minusDays(1).getMillis
    val query = QueryBuilder().query(
      BSONDocument(
        "date" -> BSONDocument("$gt" -> BSONDateTime(yesterday))
      )
    ).sort(
      "date" -> SortOrder.Descending
    )

    logs.find(query).toList(20)
  }

  def get(id: String)  = {
    logs.find(BSONDocument("_id" -> BSONObjectID(id))).headOption
  }

  def query(criteria: BSONDocument) = {
    logs.find(criteria).toList
  }
}
