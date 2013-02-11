package lib.impl

import lib.{SourceSelector, Deployer}
import com.google.inject.Inject
import uk.gov.dfid.iati.SourceMapper
import org.neo4j.graphdb.{Node => GraphNode, GraphDatabaseService}
import xml.{Node => XmlNode, XML}
import java.net.URL
import org.neo4j.tooling.GlobalGraphOperations
import collection.JavaConversions._
import uk.gov.dfid.iati.validators.IATIValidator
import concurrent.ExecutionContext.Implicits.global
import play.api.Logger

class Deployinator @Inject()(val mapper: SourceMapper[XmlNode, GraphNode], val sources: SourceSelector, val db: GraphDatabaseService, val validator: IATIValidator) extends Deployer {

  val logger = Logger.logger

  def deploy {

    // first we clear the entire graph db
    clearDb

    // load the org files then the activity files
    // this could be made simpler but i am not sure about
    // the extra memory overhead
    Seq("organisation", "activity").map { sourceType =>
      sources.get(sourceType, activeOnly = true).map { datasources =>
        datasources.partition { source =>
          val ele = XML.load(source.url)
          val version = (ele \ "@version").headOption.map(_.text).getOrElse("1.0")

          val stream = new URL(source.url).openStream
          validator.validate(stream, version, sourceType)

        } match {
          case (valid, invalid) => {
            logger.debug(s"${valid.size} valid files found")
            logger.debug(s"${invalid.size} invalid files found")

            // log the invalid files
            if (!invalid.isEmpty) {
              val invalidOutput = invalid.map(i => s"${i.title}: ${i.url}").mkString("\n")
              logger.info("Invalid Files")
              logger.info(invalidOutput)
            }

            valid.foreach(s => load(s.url))
            invalid
          }
        }
      }
    }
  }

  private def load(uri: String) {
    val url = new URL(uri)
    val ele = XML.load(url)
    mapper.map(ele)
  }

  private def clearDb {
    val tx = db.beginTx
    try {
      // kill the indexes
      db.index.nodeIndexNames.foreach(db.index.forNodes(_).delete)
      db.index.relationshipIndexNames.foreach(db.index.forRelationships(_).delete)

      // kill the nodes and relationships
      val ops = GlobalGraphOperations.at(db)
      ops.getAllRelationships.foreach(_.delete)
      ops.getAllNodes.foreach(_.delete)

      // complete the transaction
      tx.success
    } catch {
      case e: Throwable => {
        tx.failure
        logger.error(e.getMessage)
        throw e
      }
    } finally {
      tx.finish
    }
  }
}
