package uk.gov.dfid.loader

import reactivemongo.api.{DefaultCollection, DefaultDB}
import reactivemongo.bson.{BSONBoolean, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import util.Sectors
import xml.XML
import java.net.URL
import concurrent.ExecutionContext.Implicits.global
import org.neo4j.cypher.ExecutionEngine
import com.google.inject.Inject
import concurrent.{Await, Future, future}
import uk.gov.dfid.common.neo4j.GraphDatabaseManager
import uk.gov.dfid.common.api.ProjectsApi
import org.neo4j.graphdb.GraphDatabaseService
import uk.gov.dfid.common.DataLoadAuditor
import concurrent.duration.Duration

trait DataLoader {
  def load: Future[Unit]
}

class Loader @Inject()(manager: GraphDatabaseManager, mongodb: DefaultDB, auditor: DataLoadAuditor) extends DataLoader {

  def load = {
    future {
      val neo4j      = manager.restart(true)
      // val neo4j      = manager.get
      val sources    = mongodb.collection("iati-datasources")
      val engine     = new ExecutionEngine(neo4j)
      val aggregator = new Aggregator(engine, mongodb, new ProjectsApi(mongodb), auditor)
      val documents  = new DocumentAggregator(engine, mongodb, auditor)
      val projects   = new ProjectAggregator(engine, mongodb, auditor)
      val other      = new OtherOrgAggregator(engine, mongodb, auditor)
      val sectors    = new Sectors(mongodb)
      val indexer    = new Indexer(mongodb, engine, sectors)

      // drop current audtis table.  Transient data ftw
      auditor.drop
      auditor.info("Loading data")

      validateAndMap(sources, neo4j)
      aggregator.rollupCountryBudgets
      aggregator.rollupCountrySectorBreakdown
      aggregator.rollupCountryProjectBudgets
      aggregator.loadProjects
      aggregator.rollupProjectBudgets
      documents.collectProjectDocuments
      projects.collectTransactions
      projects.collectPartnerProjects
      projects.collectPartnerTransactions
      projects.collectProjectDetails
      projects.collectProjectSectorGroups
      projects.collectProjectLocations
      other.collectOtherOrganisationProjects
      other.collectTransactions

      auditor.info("Indexing Data")
      indexer.index
      auditor.success("Indexed Data")

      auditor.success("Loading process completed")
    }
  }

  private def validateAndMap(sources: DefaultCollection, neo4j: GraphDatabaseService) = {

    val validator = new Validator
    val mapper    = new Mapper(neo4j, auditor)

    auditor.info("Validating and Loading selected data sources")

    val datasources = Await.result(sources.find(BSONDocument("active" -> BSONBoolean(true))).toList, Duration.Inf)

    auditor.info(s"Fetched ${datasources.size} active data sources")
    auditor.info(s"Validating data sources (may take some time)")

    // partition by valid status
    datasources.partition { sourceDocument =>

      val source = sourceDocument.toTraversable

      // validate the data source
      val url     = source.getAs[BSONString]("url").map(_.value).get

      // validation throws uncontrollable errors
      try{
        val ele     = XML.load(url)
        val stream  = new URL(url).openStream
        val valid = validator.validate(stream, source.getAs[BSONString]("sourceType").map(_.value).get)

        if (!valid) {
          auditor.warn(s"Invalid source at $url")
        } else {
          auditor.info(s"Valid source at $url")
        }

        valid
      } catch {
        case e: Throwable => {
          auditor.error(s"Error validating $url - ${e.getMessage}")
          false
        }
      }

    } match {
      case (valid, invalid) => {

        auditor.success("All datasource validated")

        auditor.info("Mapping valid data sources (may take some time)")

        valid.foreach { validSource  =>
          val uri = validSource.getAs[BSONString]("url").map(_.value).get
          val url = new URL(uri)
          val ele = XML.load(url)

          try{
           mapper.map(ele)
          }catch {
            case e: Throwable => auditor.error(s"Error mapping data from source $uri")
          }
        }

        auditor.success("All data sources mapped")
      }
    }
  }
}
