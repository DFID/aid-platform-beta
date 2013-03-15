package uk.gov.dfid.common

import com.google.inject.Inject
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONDateTime, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import java.util.Date
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.core.commands.GetLastError


trait Auditor {
  def info(msg: String)
  def success(msg: String)
  def failure(msg: String)
  def warn(msg: String)
  def error(msg: String)
}

/**
 * Used to add responding messages to long running processes in the system
 * such as the loader
 */
class DataLoadAuditor @Inject()(db: DefaultDB) extends Auditor {

  private val audits = db.collection("dataload-audits")

  def success(msg: String) = insert("success", msg)
  def failure(msg: String) = insert("failure", msg)
  def error(msg: String)   = insert("error", msg)
  def info(msg: String)    = insert("info", msg)
  def warn(msg: String)    = insert("warn", msg)

  private def insert(msgType: String, msg: String) = {
    audits.insert(
      BSONDocument(
        "type"    -> BSONString(msgType),
        "message" -> BSONString(msg),
        "date"    -> BSONDateTime(new Date().getTime)
      ),
      GetLastError(false, None, false)
    )
  }
}
