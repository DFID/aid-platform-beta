package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.DataLoadAuditor
import reactivemongo.bson.{BSONLong, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration.Duration

class PartnerAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

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
