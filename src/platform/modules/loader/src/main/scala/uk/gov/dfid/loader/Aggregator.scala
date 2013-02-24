package uk.gov.dfid.loader

import org.joda.time.DateTime
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONLong, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentReader
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.Node

/**
 * Aggregates a bunch of data related to certain elements
 */
class Aggregator(engine: ExecutionEngine, db: DefaultDB) {

  def rollupCountryBudgets = {

    db.collection("countries").find(BSONDocument()).toList.map { countries =>

      val now = DateTime.now
      val (start, end) = if (now.getMonthOfYear < 4) {
        s"${now.getYear-1}-04-01" -> s"${now.getYear}-03-31"
      } else {
        s"${now.getYear}-04-01" -> s"${now.getYear + 1}-03-31"
      }

      countries.foreach { countryDocument =>

        val country = countryDocument.toTraversable
        val code = country.getAs[BSONString]("code").get.value

        val query = s"""
          | START  n=node:entities(type="iati-activity")
          | MATCH  n-[:`recipient-country`]-c,
          |        n-[:`reporting-org`]-org,
          |        n-[:budget]-b-[:value]-v
          | WHERE  org.ref="GB-1"
          | AND    c.code = "$code"
          | AND    v.`value-date` >= "$start"
          | AND    v.`value-date` <= "$end"
          | RETURN v.value as value
        """.stripMargin

        val result = engine.execute(query).columnAs[Long]("value")
        val totalBudget = result.toSeq.foldLeft(0L) { _ + _ }

        // update the country stats collection
        db.collection("country-stats").update(
          BSONDocument("code" -> BSONString(code)),
          BSONDocument("$set" -> BSONDocument(
            "code"        -> BSONString(code),
            "totalBudget" -> BSONLong(totalBudget)
          )),
          multi = false,
          upsert = true
        )
      }
    }
  }
}
