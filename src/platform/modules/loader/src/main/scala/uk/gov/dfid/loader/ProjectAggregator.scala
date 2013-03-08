package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.DataLoadAuditor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.bson.{BSONDateTime, BSONLong, BSONString, BSONDocument}
import concurrent.Await
import concurrent.duration.Duration
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global

class ProjectAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

  def collectTransactions = {

    val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

    auditor.info("Collecting Project Transactions")

    Await.ready(db.collection("transactions").drop, Duration.Inf)

    engine.execute(
      """
        | START  txn = node:entities(type="transaction")
        | MATCH  project-[:`related-activity`]-component-[:transaction]-txn,
        |        component-[:`reporting-org`]-org,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type
        | WHERE  project.type = 1
        | AND    org.ref      = "GB-1"
        | RETURN project.ref                                         as project,
        |        component.`iati-identifier`                         as component,
        |        COALESCE(txn.description?, type.`transaction-type`) as description,
        |        value.value                                         as value,
        |        date.`iso-date`                                     as date,
        |        type.`transaction-type`                             as type
      """.stripMargin).foreach { row =>

      val project     = row("project").asInstanceOf[String]
      val value       = row("value").asInstanceOf[Long]
      val date        = DateTime.parse(row("date").asInstanceOf[String], format)
      val transaction = row("type").asInstanceOf[String]
      val component   = row("component").asInstanceOf[String]
      val description = row("description").asInstanceOf[String]

      db.collection("transactions").insert(
        BSONDocument(
          "project"     -> BSONString(project),
          "component"   -> BSONString(component),
          "description" -> BSONString(description),
          "value"       -> BSONLong(value),
          "date"        -> BSONDateTime(date.getMillis),
          "type"        -> BSONString(transaction)
        )
      )
    }

    auditor.success("Collected Project Transactions")
  }

  def collectProjectDetails = {
    val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

    auditor.info("Getting project start and end dates")

    engine.execute(
      """
        | START n=node:entities(type="iati-activity")
        | MATCH n-[:`reporting-org`]-o,
        |       n-[:`activity-status`]-a,
        |       n-[:`activity-date`]-d
        | WHERE n.hierarchy = 1
        | AND   o.ref = "GB-1"
        | RETURN distinct(n.`iati-identifier`) as id, d.type as type, COALESCE(d.`iso-date`?, d.`activity-date`) as date
      """.stripMargin).foreach { row =>

      val id       = row("id").asInstanceOf[String]
      val dateType = row("type").asInstanceOf[String]
      val date     = DateTime.parse(row("date").asInstanceOf[String], format)

      db.collection("projects").update(
        BSONDocument("iatiId" -> BSONString(id)),
        BSONDocument("$set" -> BSONDocument(
          dateType -> BSONDateTime(date.getMillis)
        )),
        upsert = false, multi = false
      )
    }

    auditor.success("Project dates added")
  }
}
