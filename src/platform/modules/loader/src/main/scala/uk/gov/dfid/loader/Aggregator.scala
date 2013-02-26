package uk.gov.dfid.loader

import org.joda.time.DateTime
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONObjectID, BSONLong, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentReader
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.graphdb.Node
import Implicits._
import uk.gov.dfid.common.api.{Api, ProjectsApi}
import uk.gov.dfid.common.models.Project
import play.api.libs.iteratee.Enumerator

/**
 * Aggregates a bunch of data related to certain elements
 */
class Aggregator(engine: ExecutionEngine, db: DefaultDB, projects: Api[Project]) {

  def loadProjects = {

    println("Loading Projects")

    // drop the collection and start up
    db.collection("projects").drop.onComplete { case _ =>

      // find all projects in neo4j database
      val projectNodes = engine.execute(
        """
          | START  n=node:entities(type="iati-activity")
          | MATCH  n-[:`reporting-org`]-o
          | WHERE  n.hierarchy = 1
          | AND    o.ref = "GB-1"
          | RETURN n
        """.stripMargin).columnAs[Node]("n")

      // loop over each project and insert some stuff in mongo
      projectNodes.foreach { projectNode =>

        val title       = projectNode.getPropertySafe[String]("title").get
        val description = projectNode.getPropertySafe[String]("description").getOrElse("")
        val id          = projectNode.getPropertySafe[String]("iati-identifier").get
        val projectType = id match {
          case i if (countryProjectIds  contains i) => "country"
          case i if (regionalProjectIds contains i) => "regional"
          case i if (globalProjectIds   contains i) => "global"
          case _ => "undefined"
        }

        val project = Project(None, id, title, description, projectType)
        projects.insert(project)
      }

    }

    println("Loaded Projects")
  }

  def rollupCountryBudgets = {

    println("Rolling up Country Budgets")

    db.collection("countries").find(BSONDocument()).toList.map { countries =>

      val now = DateTime.now
      val (start, end) = if (now.getMonthOfYear < 4) {
        s"${now.getYear-1}-04-01" -> s"${now.getYear}-03-31"
      } else {
        s"${now.getYear}-04-01" -> s"${now.getYear + 1}-03-31"
      }

      countries.foreach { countryDocument =>

        val country = countryDocument.toTraversable

        try {
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
            | RETURN SUM(v.value) as value
          """.stripMargin

          val result = engine.execute(query).columnAs[Object]("value")

          val totalBudget = result.toSeq.head match {
            case v: java.lang.Integer => v.toLong
            case v: java.lang.Long    => v.toLong
          }

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
        catch {
          case e: Throwable => {
            println(e.getMessage)
            println(e.getStackTrace.mkString("\n\t"))
          }
        }
      }
    }
  }

  private lazy val countryProjectIds = {
    engine.execute(
      """
        | START  n=node:entities(type="iati-activity")
        | MATCH  n-[:`recipient-country`]-a,
        |        n-[:`related-activity`]-p,
        |        n-[:`reporting-org`]-o
        | WHERE  n.hierarchy=2
        | AND    p.type=1
        | AND    o.ref = "GB-1"
        | RETURN DISTINCT(p.ref) as id
      """.stripMargin).columnAs[String]("id").toSeq
  }

  private lazy val  regionalProjectIds = {
    engine.execute(
      """
        | START n=node:entities(type="iati-activity")
        | MATCH n-[r?:`recipient-region`]-a,
        |       n-[:`related-activity`]-p
        | WHERE n.hierarchy=2
        | // Parent Activity must have a
        | AND   p.type=1
        | AND   (
        |       (r is not null)
        |   OR  (
        |         (r is null)
        |     AND (
        |          n.`recipient-region`! = "Balkan Regional (BL)"
        |       OR n.`recipient-region`! = "East Africa (EA)"
        |       OR n.`recipient-region`! = "Indian Ocean Asia Regional (IB)"
        |       OR n.`recipient-region`! = "Latin America Regional (LE)"
        |       OR n.`recipient-region`! = "East African Community (EB)"
        |       OR n.`recipient-region`! = "EECAD Regional (EF)"
        |       OR n.`recipient-region`! = "East Europe Regional (ED)"
        |       OR n.`recipient-region`! = "Francophone Africa (FA)"
        |       OR n.`recipient-region`! = "Central Africa Regional (CP)"
        |       OR n.`recipient-region`! = "Overseas Territories (OT)"
        |       OR n.`recipient-region`! = "South East Asia (SQ)"
        |     )
        |   )
        | )
        | RETURN DISTINCT(p.ref) as id
      """.stripMargin).columnAs[String]("id").toSeq
  }

  private lazy val  globalProjectIds = {
    engine.execute(
      """
        | START n=node:entities(type="iati-activity")
        | MATCH n-[:`related-activity`]-p
        | WHERE n.hierarchy=2
        | AND  (n.`recipient-region`! = "Administrative/Capital (AC)"
        |    OR n.`recipient-region`! = "Non Specific Country (NS)"
        |    OR n.`recipient-region`! = "Multilateral Organisation (ZZ)")
        | AND   p.type=1
        | RETURN DISTINCT(p.ref) as id
      """.stripMargin).columnAs[String]("id").toSeq
  }
}
