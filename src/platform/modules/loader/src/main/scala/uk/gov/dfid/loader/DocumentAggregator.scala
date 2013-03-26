package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.{Auditor, DataLoadAuditor}
import reactivemongo.bson.{BSONArray, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration.Duration

class DocumentAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: Auditor) {

  def collectProjectDocuments = {

    auditor.info("Dropping documents collection")
    // drop the collection and start up
    Await.ready(db.collection("documents").drop, Duration.Inf)
    auditor.info("Documents collection dropped")

    auditor.info("Collecting Project Documents")

    engine.execute(
      """
        | START  doc = node:entities(type = "document-link")
        | MATCH  category -[:category]- doc <-[:`document-link`]- project
        | RETURN project.`iati-identifier`                 as id,
        |        doc.title!                                as title,
        |        doc.format                                as format,
        |        doc.url                                   as url,
        |        COLLECT(COALESCE(category.category?, "")) as categories
      """.stripMargin).foreach { row =>

      val projectId  = row("id").asInstanceOf[String]
      val format     = row("format").asInstanceOf[String]
      val url        = row("url").asInstanceOf[String]
      val categories = row("categories").asInstanceOf[Seq[String]]
      val title      = row("title") match {
        case null => ""
        case t    => t.asInstanceOf[String]
      }

      db.collection("documents").insert(
        BSONDocument(
          "project"    -> BSONString(projectId),
          "title"      -> BSONString(title),
          "format"     -> BSONString(format),
          "url"        -> BSONString(url),
          "categories" -> BSONArray(
            categories.map(c => BSONString(c)): _*
          )
        )
      )
    }

    auditor.success("Aggregated all project documents")
  }
}
