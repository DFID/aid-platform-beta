package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.{Auditor, DataLoadAuditor}
import reactivemongo.bson.{BSONArray, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration.Duration
import scala.Some
import uk.gov.dfid.loader.util.OtherOrganisations


class DocumentAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: Auditor) {

  def collectProjectDocuments = {

     try{

    auditor.info("Dropping documents collection")
    // drop the collection and start up
    Await.ready(db.collection("documents").drop, Duration.Inf)
    auditor.info("Documents collection dropped")

    auditor.info("Collecting Project Documents")

    val supportedOrgRefs = OtherOrganisations.Supported :+ "GB-1"
    val orgRefList = supportedOrgRefs.mkString("['","','","']")

    auditor.info(orgRefList)

    engine.execute(
      s"""
        |START  doc = node:entities(type = "document-link")
        |MATCH  category-[:category]-doc<-[:`document-link`]-project-[?:`iati-identifier`]-id,
        |       project-[:`reporting-org`]-org        
        |WHERE  HAS(org.ref) AND org.ref IN ${ orgRefList }
        |RETURN COALESCE(project.`iati-identifier`?, id.`iati-identifier`?) as id,
        |       doc.title!                                                  as title,
        |       COALESCE(doc.format?, "text/plain")                         as format,
        |       doc.url                                                     as url,
        |       COLLECT(COALESCE(category.category?, ""))                   as categories
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
    catch{
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }
  }
}