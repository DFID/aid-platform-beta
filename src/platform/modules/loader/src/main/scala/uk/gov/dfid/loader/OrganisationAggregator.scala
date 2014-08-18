package uk.gov.dfid.loader

import org.joda.time.DateTime
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.Node
import uk.gov.dfid.common.api.Api
import uk.gov.dfid.common.models.Project
import concurrent.duration._
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONLong
import uk.gov.dfid.loader.Implicits._
import reactivemongo.bson.BSONInteger
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import concurrent._
import java.util.Date
import java.text.SimpleDateFormat
import uk.gov.dfid.loader.util.Converter
import uk.gov.dfid.common.{Auditor, DataLoadAuditor}

class OrganisationAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: Auditor) {

  def loadCountryOperationPlanBudgets = {

    try {
    auditor.info("Dropping country operational plan budgets")
    // drop the collection and start up
    Await.ready(db.collection("country-operation-budgets").drop, Duration.Inf)
    auditor.info("Country Operational Plan Budgets collection dropped")

    auditor.info("Loading Country Operational Plan Budgets")

    val (start, end) = currentFinancialYear

     
      engine.execute(
      s"""
        | START  n=node:entities(type="recipient-country-budget")
        | MATCH  n-[:`recipient-country`]-rc,
        |        n-[:`period-start`]-ps,
        |        n-[:`period-end`]-pe,
        |        n-[:`value`]-v
        | WHERE  ps.`iso-date`>= "$start" 
        | AND    pe.`iso-date`<= "$end"
        | RETURN rc.`recipient-country`    as country,
        |        rc.code                   as code,
        |        ps.`iso-date`             as financialStartDate,
        |        pe.`iso-date`             as financialEndDate,
        |        v.currency                as currency,
        |        v.value                   as operationalBudget 
      """.stripMargin).toSeq.foreach { row =>

      val country             = row("country").asInstanceOf[String]
      val code                = row("code").asInstanceOf[String]
      val financialStartDate  = row("financialStartDate").asInstanceOf[String]
      val financialEndDate    = row("financialEndDate").asInstanceOf[String]
      val currency            = row("currency").asInstanceOf[String]
      val operationalBudget   = Converter.toDouble(row("operationalBudget")) 

      db.collection("country-operation-budgets").insert(
        BSONDocument(
          "country"               -> BSONString(country),
          "code"                  -> BSONString(code),
          "financialStartDate"    -> BSONString(financialStartDate),
          "financialEndDate"      -> BSONString(financialEndDate),
          "currency"              -> BSONString(currency),
          "operationalBudget"     -> BSONDouble(operationalBudget)
        )
      )
    }

    auditor.success("All Country Operational Plan loaded ")
    } catch {
      case e: Throwable => e.printStackTrace(); auditor.error(s"Error loading country operational plan: ${e.getMessage}") 
    }
    
  }

  private def currentFinancialYear = {
    val now = DateTime.now
    if (now.getMonthOfYear < 4) {
      s"${now.getYear-1}-04-01" -> s"${now.getYear}-03-31"
    } else {
      s"${now.getYear}-04-01" -> s"${now.getYear + 1}-03-31"
    }
  }
}
