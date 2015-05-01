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

class PartnerProjectAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  /* Funded/Partner Projects: Collecting Transactions */
  def collectPartnerTransactions = {

    auditor.info("Collecting Partner Project Transactions (PartnerProjectAggregator)")

    val results = db.collection("funded-projects").find(
      BSONDocument(),
      BSONDocument("funded" -> BSONInteger(1))
    ).toList

    val projects = Await.result(results, Duration Inf)
    val whereClause = projects.map(
      _.getAs[BSONString]("funded").map(_.value).get
    ).mkString("WHERE ia.`iati-identifier`? IN ['", "', '", "'] AND n.version IN [1.05,1.04,1.03,1.02,1.01]")

       engine.execute(
       s"""
        | START n=node:entities(type="iati-activities")
        | MATCH n-[:`iati-activity`]-ia,
        |       ia-[:transaction]-txn,
        |       txn-[:value]-value,
        |       txn-[:`transaction-date`]-date,
        |       txn-[:`transaction-type`]-type,
        |       txn-[r?:`receiver-org`]-receiver,
        |       txn-[p?:`provider-org`]-provider
        | $whereClause    
        | RETURN   ia.`iati-identifier`?           as id,
        |          COALESCE(txn.description?, "") as description,
        |          value.value                    as value,        
        |          COALESCE(receiver.`receiver-org`?, txn.`receiver-org`?, "") as `receiver-org`,
        |          COALESCE(provider.`provider-org`?,"") as `provider-org`,
        |          COALESCE(provider.`provider-activity-id`?,"") as `provider-activity-id`,
        |          date.`iso-date`?                as date,
        |          type.code?                      as type
       """.stripMargin).foreach { row => 

      val project          = { if(row("id").isInstanceOf[String]) row("id").asInstanceOf[String] else "" }
      
      val value            = Converter.toDouble(row("value"))
      val date             = try { DateTime.parse(row("date").asInstanceOf[String].replace('Z',' ').trim(), format).getMillis } catch { case _ : Throwable => 0 }
      val transaction      = { if(row("type").isInstanceOf[String]) row("type").asInstanceOf[String] else ""}
      val receiver         = { if(row("receiver-org").isInstanceOf[String]) row("receiver-org").asInstanceOf[String] else ""}
      val provider         = { if(row("provider-org").isInstanceOf[String]) row("provider-org").asInstanceOf[String] else ""}
      val providerActivity = { if(row("provider-activity-id").isInstanceOf[String]) row("provider-activity-id").asInstanceOf[String] else ""}
      val description      = { if(row("description").isInstanceOf[String]) row("description").asInstanceOf[String] else ""}

      db.collection("transactions").insert(
        BSONDocument(
          "project"                -> BSONString(project),
          "component"              -> BSONString(""),
          "description"            -> BSONString(description),
          "receiver-org"           -> BSONString(receiver),
          "provider-org"           -> BSONString(provider),
          "provider-activity-id"   -> BSONString(providerActivity),
          "value"                  -> BSONDouble(value),          
          "date"                   -> BSONDateTime(date),
          "type"                   -> BSONString(transaction)
        )
      )
    }
    auditor.success("Collected Partner Project Transactions (PartnerProjectAggregator)")
  }

  /* Funded/Partner Projects: Get Projects from Database*/
  def collectPartnerProjects = {

    auditor.info("Collecting Partner Projects (PartnerProjectAggregator)")

    Await.ready(db.collection("funded-projects").drop, Duration.Inf)
 
    try{
      engine.execute(s"""
                        |  START  n=node:entities(type="iati-activities")
                        |  MATCH  n-[:`iati-activity`]-ia,
                        |         ia-[:`participating-org`]-o,
                        |        ia-[:`reporting-org`]-ro,
                        |         ia-[:transaction]-t-[:`transaction-type`]-tt,
                        |         ia-[?:description]-d,
                        |         ia-[:`title`]-ti,
                        |         t-[:value]-v,
                        |         ia-[:`activity-status`]-status,
                        |         t-[:`provider-org`]-po,
                        |         ia-[?:`recipient-country`]-country,
                        |         ia-[?:`recipient-region`]-region
                        |  WHERE  n.version IN [1.05,1.04,1.03,1.02,1.01]
                        |  AND o.role  = "Funding"
                        |  AND HAS(o.ref) AND o.ref IN ${SupportedOrgRefsForPartners.Participating.mkString("['","','","']")}                      
                        |  AND    tt.code = "IF"
                        |  AND    HAS(po.`provider-activity-id`)
                        |  RETURN  n.`version`          as version, 
                        |          ia.`iati-identifier`?      as funded,
                        |          ro.`reporting-org`        as reporting   ,
                        |          ti.`title`                   as title       ,
                        |          COALESCE(d.description?, ia.description?, "") as description,
                        |          po.`provider-activity-id` as funding     ,
                        |          COALESCE(v.currency?, "GBP")  as currency,
                        |          SUM(v.value)              as funds       ,
                        |          status.code?               as status,
                        |          COALESCE(country.code?,region.code?,"")   as recipient
                     """.stripMargin).toSeq.foreach { row =>

        val funded      = { if(row("funded").isInstanceOf[String]) row("funded").asInstanceOf[String] else ""}
        val reporting   = { if(row("reporting").isInstanceOf[String]) row("reporting").asInstanceOf[String] else ""}
        val title       = { if(row("title").isInstanceOf[String]) row("title").asInstanceOf[String] else ""}
        val description = { if(row("description").isInstanceOf[String]) row("description").asInstanceOf[String] else ""}
        val funding     = { if(row("funding").isInstanceOf[String]) row("funding").asInstanceOf[String] else ""}
        
        val status      = Converter.toLong(row("status"))        
        val currency    = { if( row("currency").isInstanceOf[String] ) row("currency").asInstanceOf[String] else ""}
        val funds       = Converter.toDouble(row("funds"))

        val recipient   = row("recipient") match {          
          case v: java.lang.Long    => v.toString
          case v: java.lang.Double  => v.toString
          case v: java.lang.String => v.toString
          case _ => ""
          }

        println(s"$funding, $funded")

        val project = engine.execute(
          s"""
            | START  v=node:entities(type="iati-activity")
            | MATCH  v-[:`related-activity`]-a
            | WHERE  v.`iati-identifier`? = '$funding'
            | AND    a.type=1
            | RETURN a.ref? as id
          """.stripMargin).toSeq
             .headOption
             .map(_("id")
             .asInstanceOf[String])
             .getOrElse(funding)

        println(s"Used: $project (Recipient: $recipient)")

        // now we need to sum up the project budgets and spend.  this is not specific
        // to dfid itself.  While here we can also grab the status
        // now we need to sum up the project budgets and spend.
        val totalBudget = Converter.toDouble(engine.execute(
          s"""
             | START  funded=node:entities(type="iati-activity")
             | MATCH  funded-[:budget]-budget-[:value]-budget_value
             | WHERE  funded.`iati-identifier`? = '$funded'
             | RETURN SUM(budget_value.value) as totalBudget
          """.stripMargin).toSeq.head("totalBudget"))

        val totalSpend = Converter.toDouble(engine.execute(
          s"""
             | START  funded=node:entities(type="iati-activity")
             | MATCH  funded-[:transaction]-transaction-[:value]-transaction_value,
             | transaction-[:`transaction-type`]-type
             | WHERE  funded.`iati-identifier` = '$funded'
             | AND    HAS(type.`code`)
             | AND    (type.`code` = 'D' OR type.`code` = 'E')
             | RETURN SUM(transaction_value.value) as totalSpend
          """.stripMargin).toSeq.head("totalSpend"))

        // then we need to get the dates as well
        val dates = engine.execute(
          s"""
            | START  n=node:entities(type="iati-activity")
            | MATCH  d-[:`activity-date`]-n-[:`activity-status`]-a
            | WHERE  n.`iati-identifier`? = '$funded'
            | AND HAS(d.type) AND (HAS(d.`iso-date`) OR HAS(d.`activity-date`))
            | RETURN d.type as type, COALESCE(d.`iso-date`?, d.`activity-date`) as date
          """.stripMargin).toSeq.map { row =>

          val dateType = { if( row("type").isInstanceOf[String]) row("type").asInstanceOf[String] else ""}

          if( dateType != "")
          {
            val date     = DateTime.parse(row("date").asInstanceOf[String].replace('Z',' ').trim(),format)
            dateType -> BSONDateTime(date.getMillis)
          }
          else
            dateType -> BSONDateTime(0)
        }

        db.collection("funded-projects").insert(
          BSONDocument(
            "funded"       -> BSONString(funded),
            "funding"      -> BSONString(project),
            "title"        -> BSONString(title),
            "reporting"    -> BSONString(reporting),
            // we also want to store the reporting org as the org field
            // so we can use it in the diclaimer component
            "organisation" -> BSONString(reporting),
            "description"  -> BSONString(description),
            "funds"        -> BSONDouble(funds),
            "currency"     -> BSONString(currency),
            "totalBudget"  -> BSONDouble(totalBudget),
            "totalSpend"   -> BSONDouble(totalSpend),
            "status"       -> BSONLong(status),
            "recipient"    -> BSONString(recipient)
          ).append(dates: _*)
        )

        // put the project budgets in
        engine.execute(
          s"""
            | START  b=node:entities(type="budget")
            | MATCH  v-[:value]-b-[:budget]-n,
            |        b-[:`period-start`]-p
            | WHERE  n.`iati-identifier`? = '$funded'
            | RETURN v.value        as value,                     
            |        p.`iso-date`? as date
          """.stripMargin).foreach { row =>

          val value = Converter.toDouble(row("value"))
          val date = { if ( row("date").isInstanceOf[String] ) row("date").asInstanceOf[String].replace('Z',' ').trim() else ""}

          db.collection("project-budgets").insert(
            BSONDocument(
              "id"    -> BSONString(funded),              
              "value" -> BSONDouble(value),
              "date"  -> BSONString(date)
            )
          )
        }

        // ok now we need to work out the sector breakdown for the project
        engine.execute(
          s"""
            | START  n=node:entities(type="iati-activity")
            | MATCH  s-[:sector]-n-[:`budget`]-b-[:`value`]-v
            | WHERE  n.`iati-identifier`? = '$funded'
            | AND HAS(s.code)
            | RETURN s.code                                                as code,
            |        s.sector?                                             as name,                     
            |        COALESCE(s.percentage?, 100)                          as percentage,
            |        (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total
          """.stripMargin).foreach { row =>

          val name = row("name") match {
            case null          => None
            case value: String => Some(value)
          }
          val code        = Converter.toLong(row("code")) 
          val total       = Converter.toDouble(row("total"))

          db.collection("project-sector-budgets").insert(
            BSONDocument(
              "projectIatiId" -> BSONString(funded),
              "sectorCode"    -> BSONLong(code),              
              "sectorBudget"  -> BSONDouble(total)
            ).append(
              Seq(
                name.map("sectorName" -> BSONString(_))
              ).flatten:_*
            )
          )
        }
      }
    }catch{
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }

    auditor.success("Collected Partner Projects (PartnerProjectAggregator)")
  }
  
}
