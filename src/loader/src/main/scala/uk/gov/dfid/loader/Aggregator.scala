package uk.gov.dfid.loader

import uk.gov.dfid.common.models.{CountryStats, Country}
import org.joda.time.DateTime
import uk.gov.dfid.common.neo4j.QueryEngine
import uk.gov.dfid.common.api.Api
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONLong, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter

/**
 * Aggregates a bunch of data related to certain elements
 */
class Aggregator(engine: QueryEngine, countries: Api[Country], db: DefaultDB) {

  def rollupCountryBudgets = {

    countries.all.map { all =>
      val now = DateTime.now
      val (start, end) = if (now.getMonthOfYear < 4) {
        s"${now.getYear-1}-04-01" -> s"${now.getYear}-03-31"
      } else {
        s"${now.getYear}-04-01" -> s"${now.getYear + 1}-03-31"
      }

      all.foreach { country =>

        val query = s"""
          |START  n=node:entities(type="iati-activity")
          |MATCH  n-[:`recipient-country`]-c,
          |       n-[:`reporting-org`]-org,
          |       n-[:budget]-b-[:value]-v
          |WHERE  org.ref="GB-1"
          |AND    c.code = "${country.code}"
          |AND    v.`value-date` >= "$start"
          |AND    v.`value-date` <= "$end"
          |RETURN v.value as value
        """.stripMargin

        val result = engine.execute(query).columnAs[Long]("value")
        val totalBudget = result.foldLeft(0L)(_ + _)

        // update the country stats collection
        db.collection("country-stats").update(
          BSONDocument("code" -> BSONString(country.code)),
          BSONDocument("$set" -> BSONDocument(
            "code"        -> BSONString(country.code),
            "totalBudget" -> BSONLong(totalBudget)
          )),
          multi = false,
          upsert = true
        )
      }
    }
  }
}
