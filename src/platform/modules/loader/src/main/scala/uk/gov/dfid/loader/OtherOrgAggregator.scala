package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.DataLoadAuditor
import concurrent.duration.Duration
import concurrent.Await
import reactivemongo.bson.{BSONDateTime, BSONLong, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class OtherOrgAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor)  {

  private val OTHER_ORGS = Seq("GB-4")
  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  def collectOtherOrganisationProjects = {

    auditor.info("Collecting other Organisation projects")

    Await.ready(db.collection("other-org-projects").drop(), Duration.Inf)

    engine.execute(
      s"""
        | START  activity = node:entities(type="iati-activity")
        | MATCH  status-[:`activity-status`]-activity-[:`reporting-org`]-org,
        | 	     activity-[?:title]-title,
        |        activity-[?:description]-description
        | WHERE  HAS(org.ref) AND org.ref IN ${OTHER_ORGS.mkString("['","','","']")}
        | RETURN COALESCE(activity.title?, title.title)                   AS title,
        |        COALESCE(activity.description?, description.description) AS description,
        |        activity.`iati-identifier`                               AS id,
        |        status.code                                              AS status
      """.stripMargin).foreach { row =>

      val title       = row("title").asInstanceOf[String]
      val description = row("description").asInstanceOf[String]
      val id          = row("id").asInstanceOf[String]
      val status      = row("status").asInstanceOf[Long]

      db.collection("other-org-projects").insert(
        BSONDocument(
          "title"       -> BSONString(title),
          "description" -> BSONString(description),
          "iatiId"      -> BSONString(id),
          "status"      -> BSONLong(status)
        )
      )
    }

    auditor.info("Collected other Organisation projects")
  }

  def collectTransactions = {

    auditor.info("Collecting other Organisation Project Transactions")

    Await.ready(db.collection("transactions").drop, Duration Inf)

    engine.execute(
      s"""
        | START  txn = node:entities(type="transaction")
        | MATCH  project-[:`related-activity`]-component-[:transaction]-txn,
        |        component-[:`reporting-org`]-org,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type
        | WHERE  project.type = 1
        | AND    HAS(org.ref) AND org.ref IN ${OTHER_ORGS.mkString("['","','","']")}
        | RETURN project.ref                    as project,
        |        component.`iati-identifier`    as component,
        |        COALESCE(txn.description?, "") as description,
        |        value.value                    as value,
        |        date.`iso-date`                as date,
        |        type.code                      as type
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

    auditor.success("Collected other Organisation Project Transactions")
  }
}
