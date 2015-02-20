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
import uk.gov.dfid.loader.util.SupportedOrgRefsForPartners
import uk.gov.dfid.loader.util.Converter

class ProjectAggregator201(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  /* DFID Projects: Collecting Transactions */
  /* NO transactions are loaded into Neo4J for 2.01 so Cypher can't be tested at this stage.

  def collectTransactions = {

    auditor.info("Collecting Project Transactions")

    Await.ready(db.collection("transactions").drop, Duration Inf)

    engine.execute(
      s"""
        | START  txn = node:entities(type="transaction")
        | MATCH  project-[:`related-activity`]-component-[:transaction]-txn,
        |        component-[:`reporting-org`]-org,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type,
        |        txn-[r?:`receiver-org`]-receiver
        | WHERE  project.type = 1
        | AND    HAS(org.ref) AND org.ref IN ${SupportedOrgRefsForPartners.Reporting.mkString("['","','","']")}
        | RETURN project.ref                    as project,
        |        component.`iati-identifier`?   as component,
        |        COALESCE(txn.description?, "") as description,
        |        COALESCE(component.title?, "") as title,
        |        COALESCE(receiver.`receiver-org`?, txn.`receiver-org`?, "") as `receiver-org`,        
        |        value.value                    as value,
        |        date.`iso-date`                as date,
        |        type.code                      as type
      """.stripMargin).foreach { row =>

      val project     = { if(row("project").isInstanceOf[String]) row("project").asInstanceOf[String] else "" }

      val value       = Converter.toDouble(row("value"))
      
      val date        = DateTime.parse(row("date").asInstanceOf[String], format) 
      val transaction = { if(row("type").isInstanceOf[String]) row("type").asInstanceOf[String] else "" }
      val component   = { if(row("component").isInstanceOf[String]) row("component").asInstanceOf[String] else "" }
      val description = { if(row("description").isInstanceOf[String]) row("description").asInstanceOf[String] else "" }
      val receiver    = { if(row("receiver-org").isInstanceOf[String]) row("receiver-org").asInstanceOf[String] else ""}
      val title       = { if(row("title").isInstanceOf[String]) row("title").asInstanceOf[String] else ""}

      db.collection("transactions").insert(
        BSONDocument(
          "project"       -> BSONString(project),
          "component"     -> BSONString(component),
          "description"   -> BSONString(description),
          "receiver-org"  -> BSONString(receiver),
          "title"         -> BSONString(title),
          "value"         -> BSONDouble(value),          
          "date"          -> BSONDateTime(date.getMillis),
          "type"          -> BSONString(transaction)
        )
      )
    }

    auditor.success("Collected Project Transactions")
  } */

  /* DFID Projects: Collect project start and end Dates */
  def collectProjectDetails = {
    val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

    auditor.info("Getting project start and end dates")

    engine.execute(
      s"""
        | START n=node:entities(type="iati-activity")
        | MATCH n-[:`reporting-org`]-o,
        |       n-[:`activity-status`]-a,
        |       n-[:`activity-date`]-d
        | WHERE n.hierarchy! = 1
        | AND   HAS(o.ref) AND o.ref IN ${SupportedOrgRefsForPartners.Reporting.mkString("['","','","']")}
        | RETURN distinct(n.`iati-identifier`?) as id, d.type as type, COALESCE(d.`iso-date`?, d.`activity-date`) as date
      """.stripMargin).foreach { row =>

      val id       = { if(row("id").isInstanceOf[String]) row("id").asInstanceOf[String] else "" }
      val dateType = { if(row("type").isInstanceOf[String]) row("type").asInstanceOf[String] else "" }

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

    def dateTypeConverter(dateType) {
      switch (dateType) {
        case:"1" return "start-actual"; break;
      }
    }

  
  /* DFID Projects: Getting Project Sector Groups */
  def collectProjectSectorGroups = {

    Await.ready(db.collection("project-sector-budgets").drop, Duration.Inf)

    val projectSectorBudgets = db.collection("project-sector-budgets")
    auditor.info("Getting project sector groups")
    try {
      engine.execute(
        s"""
          | START  n=node:entities(type="iati-activity")
          | MATCH  n-[:`reporting-org`]-o,
          |   	   n-[:`related-activity`]-a,
          |        n-[:`budget`]-b-[:`value`]-v,
          |        n-[:`sector`]-s,
          |        b-[:`period-start`]-p
          | WHERE  n.hierarchy! = 2
          | AND    HAS(o.ref) AND o.ref IN ${SupportedOrgRefsForPartners.Reporting.mkString("['","','","']")}
          | AND	   a.type = 1
          | RETURN a.ref as id, s.code as code, s.sector as name,
          |        COALESCE(s.percentage?, 100) as percentage, sum(v.value) as val,
          |        (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total,
          |        p.`iso-date` as date
          | ORDER BY id asc, total desc
        """.stripMargin).foreach { row =>

        val id          = { if(row("id").isInstanceOf[String]) row("id").asInstanceOf[String] else "" }
        val name        = { if(row("name").isInstanceOf[String]) row("name").asInstanceOf[String] else "" }

        val code        = Converter.toLong(row("code"))
        val total       = Converter.toDouble(row("total"))

        val date        = { if(row("date").isInstanceOf[String]) row("date").asInstanceOf[String] else "" }

        projectSectorBudgets.insert(
          BSONDocument(
            "projectIatiId" -> BSONString(id),
            "sectorName"    -> BSONString(name),
            "sectorCode"    -> BSONLong(code),
            "sectorBudget"  -> BSONDouble(total),
            "date"          -> BSONString(date)
          )
        )
      }
      auditor.success("Collected project sector groups")
    } catch {
      case e: Throwable => println(e.getMessage); println(e.getStackTraceString)
    }
  }

  /* DFID Projects: Insert Project Locations */

  def collectProjectLocations = {

    auditor.info("Collecting project locations")

    try{

    // drop the collection as we will build is all from scratch here
    Await.ready(db.collection("locations").drop, Duration.Inf)

    engine.execute(
      s"""
        | START  location=node:entities(type='location')
        | MATCH  org-[:`reporting-org`]-project-[:location]-location,
        |        location-[:point]-point,
        |        location-[:exactness]-exactness,
        |        location-[:`feature-designation`]-type
        | WHERE  HAS(org.ref) AND org.ref IN ${SupportedOrgRefsForPartners.Reporting.mkString("['","','","']")}
        | RETURN project.`iati-identifier`? as id,
        |        project.title             as title,
        |        location.name             as name,
        |        point.pos                 as pos,
        |        exactness.code            as precision,
        |        type.code                 as type
      """.stripMargin).foreach { row =>

      val id           = { if ( row("id").isInstanceOf[String] ) row("id").asInstanceOf[String] else "" }
      val title        = { if ( row("title").isInstanceOf[String] ) row("title").asInstanceOf[String] else "" }
      val name         = { if ( row("name").isInstanceOf[String] ) row("name").asInstanceOf[String] else "" }
      val precision    = { if ( row("precision").isInstanceOf[Long] ) row("precision").asInstanceOf[Long] else 0 }
      
      val pos = row("pos").asInstanceOf[String].trim
      val locationType = { if ( row("type").isInstanceOf[String] ) row("type").asInstanceOf[String] else "" }

      if(pos != "" && pos.contains(" ")){
        
        val latitude    = Converter.toDouble(pos.substring(0, pos.indexOf(" ")))
        val longitude     = Converter.toDouble(pos.substring(pos.indexOf(" ") + 1, pos.length ))

        db.collection("locations").insert(BSONDocument(
        "id"        -> BSONString(id),
        "title"     -> BSONString(title),
        "name"      -> BSONString(name),
        "precision" -> BSONLong(precision),
        "longitude" -> BSONDouble(longitude),
        "latitude"  -> BSONDouble(latitude),
        "type"      -> BSONString(locationType)
        ))
        
      }
      
    }   

    }
    catch{
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }

    auditor.success("Collected all project locations")
  }  
}
