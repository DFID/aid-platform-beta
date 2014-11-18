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
  def loadSeparate : Future[Unit]
}

class Loader @Inject()(manager: GraphDatabaseManager, mongodb: DefaultDB, auditor: DataLoadAuditor) extends DataLoader {

  def load = {
    future {
      val neo4j      = manager.restart(true)
      //val neo4j      = manager.get
      val sources    = mongodb.collection("iati-datasources")
      val engine     = new ExecutionEngine(neo4j)
      val aggregator = new Aggregator(engine, mongodb, new ProjectsApi(mongodb), auditor)
      val documents  = new DocumentAggregator(engine, mongodb, auditor)
      val organisations  = new OrganisationAggregator(engine, mongodb, auditor)
      val projects   = new ProjectAggregator(engine, mongodb, auditor)
      val other      = new OtherOrgAggregator(engine, mongodb, auditor)
      val separate   = new SeparateDataAggregator(engine, mongodb, auditor)
      val sectors    = new Sectors(mongodb)
      val indexer    = new Indexer(mongodb, engine, sectors, auditor)
      val results    = new CountryResults(engine, mongodb, auditor)
      val sector_hierarchies_results  = new SectorHierarchies(engine, mongodb, auditor)

       val aggregatorBackwardCompatibility = new AggregatorBackwardCompatibility(engine, mongodb, auditor)

      // drop current audtis table.  Transient data ftw
      auditor.drop
      auditor.info("Loading data")

      val timeCRStart = System.currentTimeMillis
      results.loadCountryResults

      val timeSHStart = System.currentTimeMillis
     // sector_hierarchies_results.loadSectorHierarchies

      val timeVMStart = System.currentTimeMillis
      validateAndMap(sources, neo4j)


      val timeOrgStart = System.currentTimeMillis
      organisations.loadCountryOperationPlanBudgets

      val timeAggStart = System.currentTimeMillis
      aggregator.rollupCountryBudgets
      aggregator.rollupCountrySectorBreakdown
      aggregator.rollupCountryProjectBudgets
      aggregator.loadProjects
      aggregator.rollupProjectBudgets

      val timeDocStart = System.currentTimeMillis
      documents.collectProjectDocuments

      val timePrjStart = System.currentTimeMillis
      projects.collectProjectSectorGroups
      projects.collectTransactions
      projects.collectPartnerProjects
      projects.collectPartnerTransactions
      projects.collectProjectDetails
      projects.collectProjectLocations

      val timeOGDStart = System.currentTimeMillis
      other.collectOtherOrganisationProjects
      other.collectTransactions

      aggregatorBackwardCompatibility.collectProjectLocationsForVersionBefore104

      val timeSepDataStart = System.currentTimeMillis
      separate.mergeSeparatelyLoadedProjects

      val timeIndexStart = System.currentTimeMillis
      indexer.index
      val end = System.currentTimeMillis

      auditor.success("Loading process completed")
      auditor.success("Load performance in milliSecs:: ")
      auditor.success("Country Result:: " + (timeVMStart-timeCRStart) )
      auditor.success("Mapping and Validation:: " + (timeOrgStart-timeVMStart) )
      auditor.success("Country Operation Plan:: " + (timeAggStart-timeOrgStart) )
      auditor.success("Aggregation:: " + (timeDocStart-timeAggStart) )
      auditor.success("Project Documents:: " + (timePrjStart-timeDocStart) )
      auditor.success("Partner project, sector, transaction, locations:: " + (timeOGDStart-timePrjStart) )
      auditor.success("OGD Projects and Transactions:: " + (timeSepDataStart-timeOGDStart) )
      auditor.success("Merging separately loaded projects and transactions:: " + (timeSepDataStart-timeIndexStart) )
      auditor.success("Indexing in Elastic Search:: " + (end-timeIndexStart) )

      auditor.success("Total Load Time:: " + (end-timeCRStart) )
    }
  }

  def loadSeparate = {

    future {
      auditor.info("Separate Loading process has started")

      val neo4j      = manager.restart(true)
      //val neo4j      = manager.get
      val sources    = mongodb.collection("iati-datasources")
      val engine     = new ExecutionEngine(neo4j)
      val separateDataAggregator = new SeparateDataAggregator(engine, mongodb, auditor)
      
      // drop current audtis table.  Transient data ftw
      auditor.drop
      auditor.info("Loading data for FCO etc")

      val timeVMStart = System.currentTimeMillis
      validateAndMap(sources, neo4j)

      val timeOGDStart = System.currentTimeMillis
      //load data for FCO etc
      separateDataAggregator.collectProjects
      separateDataAggregator.collectTransactions
      separateDataAggregator.collectProjectDocuments
      separateDataAggregator.collectProjectLocations

      auditor.success("Loading process completed for FCO etc")
      auditor.success("Load performance in milliSecs:: ")
      auditor.success("Projects, Transactions, Documents and locations:: " + (timeOGDStart-timeVMStart) )

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
        val valid = Seq("1.04", "1.03", "1.02","1.01") exists { version =>
          val stream  = new URL(url).openStream
          val valid = validator.validate(stream, version, source.getAs[BSONString]("sourceType").map(_.value).get)

          valid match {
            case true => { 
                          println(s"Valid for $version")
                          updateIatiVersionNumber(sources, version, url)
                        }
            case false =>println(s"Invalid for $version")
          }

          valid
        }

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

  private def updateIatiVersionNumber(sources: DefaultCollection, version: String, url: String) {

    auditor.info(s"Updating version number $version to $url")
    try { 
        sources.update(
            BSONDocument("url" -> BSONString(url)),
            BSONDocument("$set" -> BSONDocument(
              "version" -> BSONString(version)
            )),
            upsert = false, multi = false
        )
    } catch {
      case e: Exception => auditor.error(s"Failed to update version number $url")
    }  

  }

  //TODO: Use this merge collections. Example:  // documents = documents + documents-separate
  // Till not implemented do the merge in mongo script like following:
  // db['documents-separate'].copyTo('documents')
  private def mergeCollections = {
    
  }
}
