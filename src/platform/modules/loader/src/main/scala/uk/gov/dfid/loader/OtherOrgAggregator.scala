package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.DataLoadAuditor
import concurrent.duration.Duration
import concurrent.Await
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONDateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONString
import scala.Some
import uk.gov.dfid.loader.util.OtherOrganisations

class OtherOrgAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor)  {

  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  def collectOtherOrganisationProjects = {

    auditor.info("Collecting other Organisation projects")

    Await.ready(db.collection("other-org-projects").drop(), Duration.Inf)

    try {
      engine.execute(
        s"""
          | START  activity = node:entities(type="iati-activity")
          | MATCH  status-[:`activity-status`]-activity-[:`reporting-org`]-org,
          | 	     activity-[?:title]-title,
          |        activity-[?:description]-description,
          |        activity-[?:`iati-identifier`]-id
          | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.Supported.mkString("['","','","']")}
          | RETURN COALESCE(activity.title?, title.title)                       AS title,
          |        COALESCE(activity.description?, description.description)     AS description,
          |        COALESCE(activity.`iati-identifier`?, id.`iati-identifier`?) AS id,
          |        org.`reporting-org`                                          AS organisation,
          |        status.code                                                  AS status
        """.stripMargin).foreach { row =>

        val title        = Option(row("title").asInstanceOf[String]).getOrElse("")
        val description  = Option(row("description").asInstanceOf[String]).getOrElse("")
        val id           = row("id").asInstanceOf[String]
        val status       = row("status").asInstanceOf[Long]
        val organisation = row("organisation").asInstanceOf[String]

        // some data generation results in bad data being spat out.  If there is no IATI ID
        // then we are going to ignore this.
        if(id != null) {

          try{
            // now we need to sum up the project budgets and spend.
            val totalBudget = engine.execute(
              s"""
               | START  funded=node:entities(type="iati-activity")
               | MATCH  id-[r?:`iati-identifier`]-funded-[:budget]-budget-[:value]-budget_value
               | WHERE  ((r is null)     AND (funded.`iati-identifier` = '$id'))
               | OR     ((r is not null) AND (id.`iati-identifier` = '$id'))
               | RETURN SUM(budget_value.value) as totalBudget
            """.stripMargin).toSeq.head("totalBudget") match {
              case v: java.lang.Integer => v.toLong
              case v: java.lang.Long    => v.toLong
            }

            val totalSpend = engine.execute(
              s"""
               | START  funded=node:entities(type="iati-activity")
               | MATCH  id-[r?:`iati-identifier`]-funded-[:transaction]-transaction-[:value]-transaction_value,
               |        transaction-[:`transaction-type`]-type
               | WHERE  (
               |             ((r is null)     AND (funded.`iati-identifier` = '$id'))
               |          OR ((r is not null) AND (id.`iati-identifier` = '$id'))
               |        )
               | AND    (type.`code` = 'D' OR type.`code` = 'E')
               | RETURN SUM(transaction_value.value) as totalSpend
            """.stripMargin).toSeq.head("totalSpend") match {
              case v: java.lang.Integer => v.toLong
              case v: java.lang.Long    => v.toLong
            }

            // then we need to get the dates as well
            val dates = engine.execute(
              s"""
              | START  n=node:entities(type="iati-activity")
              | MATCH  d-[:`activity-date`]-n-[:`activity-status`]-a,
              |        id-[r?:`iati-identifier`]-n
              | WHERE  (
              |             ((r is null)     AND (n.`iati-identifier` = '$id'))
              |          OR ((r is not null) AND (id.`iati-identifier` = '$id'))
              |        )
              | RETURN d.type as type, COALESCE(d.`iso-date`?, d.`activity-date`) as date
            """.stripMargin).toSeq.map { row =>

              val dateType = row("type").asInstanceOf[String]
              val date     = DateTime.parse(row("date").asInstanceOf[String], format)

              dateType -> BSONDateTime(date.getMillis)
            }

            db.collection("other-org-projects").insert(
              BSONDocument(
                "title"             -> BSONString(title),
                "description"       -> BSONString(description),
                "iatiId"            -> BSONString(id),
                "status"            -> BSONLong(status),
                "totalBudget"       -> BSONLong(totalBudget),
                "organisation"      -> BSONString(organisation),
                "totalProjectSpend" -> BSONLong(totalSpend)
              ).append(dates:_*)
            )

            // put the project budgets in
            engine.execute(
              s"""
              | START  b=node:entities(type="budget")
              | MATCH  v-[:value]-b-[:budget]-n-[r?:`iati-identifier`]-id,
              |        b-[:`period-start`]-p
              | WHERE  (
              |             ((r is null)     AND (n.`iati-identifier` = '$id'))
              |          OR ((r is not null) AND (id.`iati-identifier` = '$id'))
              |        )
              | RETURN v.value      as value,
              |        p.`iso-date` as date
            """.stripMargin).foreach { row =>

              val value = row("value").asInstanceOf[Long].toInt
              val date = row("date").asInstanceOf[String]

              db.collection("project-budgets").insert(
                BSONDocument(
                  "id"    -> BSONString(id),
                  "value" -> BSONInteger(value),
                  "date"  -> BSONString(date)
                )
              )
            }

            engine.execute(
              s"""
            | START  n=node:entities(type="iati-activity")
            | MATCH  n-[?:`iati-identifier`]-id,
            |        n-[:`reporting-org`]-org,
            |        s-[:`sector`]-n-[:`budget`]-b-[:`value`]-v
            | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.Supported.mkString("['","','","']")}
            |        AND HAS(s.code) AND s.code <> ""
            | RETURN COALESCE(n.`iati-identifier`?, id.`iati-identifier`?) AS id,
            |        s.code                                               as code,
            |        s.sector?                                             as name,
            |        COALESCE(s.percentage?, 100)                          as percentage,
            |        (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total
          """.stripMargin).foreach { row =>

              val projectIatiId           = row("id").asInstanceOf[String]

              val sectorName = row("name") match {
                case null          => None
                case value: String => Some(value)
              }
              val sectorCode        = row("code").asInstanceOf[Long]

              val total       = row("total")  match {
                case v: java.lang.Integer => v.toLong
                case v: java.lang.Long    => v.toLong
                case v: java.lang.Double  => v.toLong
              }

              db.collection("project-sector-budgets").insert(
                BSONDocument(
                  "projectIatiId" -> BSONString(projectIatiId),
                  "sectorCode"  -> BSONLong(sectorCode.toLong),
                  "sectorBudget"  -> BSONLong(total)
                ).append(
                  Seq(
                    sectorName.map("sectorName" -> BSONString(_))
                  ).flatten:_*
                )
              )
            }

          }catch{
            case e: Throwable => println(e.getMessage); e.printStackTrace()
          }
        }
      }
  } catch {
    case e: Throwable => println(e.getMessage); e.printStackTrace()
  }

    auditor.info("Collected other Organisation projects")
  }

  def collectTransactions = {

    auditor.info("Collecting other Organisation Project Transactions")

    engine.execute(
      s"""
        | START  txn = node:entities(type="transaction")
        | MATCH  org-[:`reporting-org`]-project-[:transaction]-txn,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type,
        |        project-[?:`iati-identifier`]-id
        | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.Supported.mkString("['","','","']")}
        | RETURN COALESCE(project.`iati-identifier`?, id.`iati-identifier`?) as project,
        |        COALESCE(txn.description?, "")                              as description,
        |        value.value                                                 as value,
        |        date.`iso-date`                                             as date,
        |        type.code                                                   as type
      """.stripMargin).foreach { row =>

      val project     = row("project").asInstanceOf[String]
      val value       = row("value").asInstanceOf[Long]
      val date        = DateTime.parse(row("date").asInstanceOf[String], format)
      val transaction = row("type").asInstanceOf[String]
      val description = row("description").asInstanceOf[String]

      db.collection("transactions").insert(
        BSONDocument(
          "project"     -> BSONString(project),
          "description" -> BSONString(description),
          "component"   -> BSONString(""),
          "value"       -> BSONLong(value),
          "date"        -> BSONDateTime(date.getMillis),
          "type"        -> BSONString(transaction)
        )
      )
    }

    auditor.success("Collected other Organisation Project Transactions")
  }
}
