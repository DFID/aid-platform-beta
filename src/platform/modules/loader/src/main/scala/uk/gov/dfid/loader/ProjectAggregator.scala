package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import uk.gov.dfid.common.DataLoadAuditor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.bson._
import concurrent.Await
import concurrent.duration.Duration
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONDateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONString

class ProjectAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  def collectPartnerTransactions = {

    val results = db.collection("funded-projects").find(
      BSONDocument(),
      BSONDocument("funded" -> BSONInteger(1))
    ).toList

    val projects = Await.result(results, Duration Inf)
    val whereClause = projects.map(
      _.getAs[BSONString]("funded").map(_.value).get
    ).mkString("WHERE n.`iati-identifier` IN ['", "', '", "']")

    engine.execute(
      s"""
        | START n=node:entities(type="iati-activity")
        | MATCH n-[:transaction]-txn,
        |       txn-[:value]-value,
        |       txn-[:`transaction-date`]-date,
        |       txn-[:`transaction-type`]-type
        | $whereClause
        | RETURN n.`iati-identifier`            as id,
        |        COALESCE(txn.description?, "") as description,
        |        value.value                    as value,
        |        date.`iso-date`                as date,
        |        type.code                      as type
      """.stripMargin).foreach { row =>

      val project     = row("id").asInstanceOf[String]
      val value       = row("value").asInstanceOf[Long]
      val date        = DateTime.parse(row("date").asInstanceOf[String], format)
      val transaction = row("type").asInstanceOf[String]
      val component   = ""
      val description = row("description").asInstanceOf[String]

      println(s"inserting: $project $value $description")
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
  }

  def collectTransactions = {

    auditor.info("Collecting Project Transactions")

    Await.ready(db.collection("transactions").drop, Duration Inf)

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

  def collectProjectSectorGroups = {

    val projects = db.collection("projects")
    // set default values for all the projects
auditor.info("set default values for all the projects")
    projects.update(
      BSONDocument(),
      BSONDocument("$set" -> BSONDocument(
        "sectorBudget" -> BSONLong(0),
        "sectorGroups" -> BSONArray()
      )
      ), multi = true
    )
auditor.info("set default values for all the projects - end")
    auditor.info("Getting project sector groups")

    engine.execute(
      """
        | START  n=node:entities(type="iati-activity")
        | MATCH  n-[:`reporting-org`]-o,
        |   	   n-[:`related-activity`]-a,
        |        n-[:`budget`]-b-[:`value`]-v,
        |        n-[:`sector`]-s
        | WHERE  n.hierarchy = 2
        | AND    o.ref = "GB-1"
        | AND	   a.type = 1
        | RETURN a.ref as id, s.sector as name, COALESCE(s.percentage?, 100) as percentage, sum(v.value) as val,
        |        (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total
        | ORDER BY id asc, total desc
      """.stripMargin).foreach { row =>
        val id    = row("id").asInstanceOf[String]
        val name  = row("name").asInstanceOf[String]
auditor.info("id: " + id + " name: " + name)
        val total = row("total")  match {
          case v: java.lang.Integer => v.toLong
          case v: java.lang.Long    => v.toLong
        }
auditor.info("update")
        projects.update(
          BSONDocument("iatiId" -> BSONString(id)),
          BSONDocument(
            "$inc"  -> BSONDocument("sectorBudget" -> BSONLong(total)),
            "$push" -> BSONDocument(
              "sectorGroups" -> BSONDocument(
                "name" -> BSONString(name),
                "percentage" -> BSONLong(total)
              )
            )
          ), upsert = false, multi = false
        )
auditor.info("update - done")
      }

    auditor.success("Collected project sector groups")
  }
  
  def collectPartnerProjects = {

    auditor.info("Collecting Partner Projects")

    Await.ready(db.collection("funded-projects").drop, Duration.Inf)

    engine.execute("""
       | START  n=node:entities(type="iati-activity")
       | MATCH  n-[:`participating-org`]-o,
       |        n-[:`reporting-org`]-ro,
       |        n-[:transaction]-t-[:`transaction-type`]-tt,
       |        n-[:description]-d,
       |        n-[:title]-ttl,
       |        t-[:value]-v,
       |        t-[:`provider-org`]-po
       | WHERE  o.role  = "Funding"
       | AND    o.ref   = "GB-1"
       | AND    tt.code = "IF"
       | RETURN n.`iati-identifier`       as funded      ,
       |        ro.`reporting-org`        as reporting   ,
       |        ttl.title                 as title       ,
       |        d.description             as description ,
       |        po.`provider-activity-id` as funding     ,
       |        SUM(v.value)              as funds
     """.stripMargin).toSeq.foreach { row =>

      val funded      = row("funded").asInstanceOf[String]
      val reporting   = row("reporting").asInstanceOf[String]
      val title       = row("title").asInstanceOf[String]
      val description = row("description").asInstanceOf[String]
      val funding     = row("funding").asInstanceOf[String]
      val funds       = row("funds") match {
        case v: java.lang.Integer => v.toLong
        case v: java.lang.Long    => v.toLong
      }

      val project = engine.execute(
        s"""
          | START  v=node:entities(type="iati-activity")
          | MATCH  v-[:`related-activity`]-a
          | WHERE  v.`iati-identifier` = '$funding'
          | AND    a.type=1
          | RETURN a.ref as id
        """.stripMargin).toSeq.head("id").asInstanceOf[String]

      db.collection("funded-projects").insert(
        BSONDocument(
          "funded"      -> BSONString(funded),
          "funding"     -> BSONString(project),
          "title"       -> BSONString(title),
          "reporting"   -> BSONString(reporting),
          "description" -> BSONString(description),
          "funds"       -> BSONLong(funds)
        )
      )
    }

    auditor.success("Collected Partner Projects")
  }
}
