package uk.gov.dfid.loader

import com.google.inject.Inject
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import uk.gov.dfid.loader.util.{Sectors, Statuses}
import java.text.NumberFormat
import java.util.Locale
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONInteger
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONString
import scala.Some
import org.neo4j.cypher.ExecutionEngine
import uk.gov.dfid.common.ElasticSearch
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Performs indexing of elastic search data against the aggregated data
 */
class Indexer @Inject()(db: DefaultDB, engine: ExecutionEngine, sectors: Sectors) {

  def index {
    // clear the elastic search
    ElasticSearch.reset

    // perform the various indexing activities
    println("Indexing DFID Projects")
    indexDfidProjects
    println("Indexing Country Suggestions")
    indexCountrySuggestions
    println("Indexing Other Org Projects")
    indexOtherOrganisationProjects
    println("Indexing Partner Projects")
    indexPartnerProjects
  }

  private lazy val components = {
    engine.execute(
      """
        | START  component=node:entities(type="iati-activity")
        | MATCH  component-[:`related-activity`]-project,
        |        component-[?:sector]-sector,
        |        component-[?:`recipient-region`]-region,
        |        component-[?:`recipient-country`]-country,
        |        component-[:`participating-org`]-org,
        |        component-[?:`iati-identifier`]-id
        | WHERE  project.type = 1
        | RETURN COLLECT(DISTINCT(COALESCE(component.`iati-identifier`?, id.`iati-identifier`?))) as ids,
        |        project.ref                                    as parent,
        |        COLLECT(DISTINCT(sector.code))                 as sectors,
        |        COLLECT(DISTINCT(region.`recipient-region`))   as regions,
        |        COLLECT(DISTINCT(country.`recipient-country`)) as countries,
        |        COLLECT(DISTINCT(org.`participating-org`?))    as organizations
      """.stripMargin).foldLeft(Map[String, Map[String, List[String]]]()) { (memo, row) =>

      memo + (row("parent").asInstanceOf[String] -> Map(
        "subActivities" -> row("ids").asInstanceOf[List[String]].distinct,
        "sectors"       -> row("sectors").asInstanceOf[List[Long]].map(sectors.getHighLevelSector(_)).distinct,
        "regions"       -> row("regions").asInstanceOf[List[String]].distinct,
        "countries"     -> row("countries").asInstanceOf[List[String]].distinct,
        "organizations" -> row("organizations").asInstanceOf[List[String]].distinct.filterNot(_ == "UNITED KINGDOM")
      ))
    }
  }

  private def indexPartnerProjects = {
    for(
      projects <- db.collection("funded-projects").find(BSONDocument()).toList
    ) yield {

      projects.foreach { doc =>
        val funded = doc.getAs[BSONString]("funded").get.value
        val funding = doc.getAs[BSONString]("funding").get.value
        val budget = doc.getAs[BSONLong]("totalBudget").get.value
        val formattedBudget = NumberFormat.getCurrencyInstance(Locale.UK).format(budget)

        // TODO: James Hughes 22 Apr 2012 - need to get a list of all countries
        // TODO: James Hughes 22 Apr 2012 - need to get a list of all regions
        // TODO: James Hughes 22 Apr 2012 - need to get a list of all sectors

        val bean = Map(
          "id"              -> funded,
          "other"           -> funding,
          "title"           -> doc.getAs[BSONString]("title").get.value,
          "description"     -> doc.getAs[BSONString]("description").get.value,
          "status"          -> Statuses.get(doc.getAs[BSONLong]("status").get.value).get,
          "budget"          -> budget,
          "formattedBudget" -> formattedBudget.substring(0, formattedBudget.size - 3),
          "organizations"   -> (doc.getAs[BSONString]("reporting").get.value :: Nil).distinct.filterNot(_ == "UNITED KINGDOM").mkString("#"),
          "countries"       -> Nil.mkString("#"),
          "regions"         -> Nil.mkString("#"),
          "sectors"         -> Nil.mkString("#"),
          "reporting"       -> doc.getAs[BSONString]("reporting").get.value,
          "end-date"        -> chooseBetterDate( doc.getAs[BSONDateTime]("end-actual"), doc.getAs[BSONDateTime]("end-planned")),
          "start-date"      -> chooseBetterDate(doc.getAs[BSONDateTime]("start-actual"),doc.getAs[BSONDateTime]("start-planned"))
        )

        ElasticSearch.index(bean, "aid")
      }
    }
  }

  private def indexOtherOrganisationProjects = {
    for(
      projects <- db.collection("other-org-projects").find(BSONDocument()).toList
    ) yield {
      println(s"Found: $projects")
      projects.foreach { doc =>

        val id = doc.getAs[BSONString]("iatiId").get.value
        val budget = doc.getAs[BSONLong]("totalBudget").map(_.value).getOrElse(0L)
        val formattedBudget = NumberFormat.getCurrencyInstance(Locale.UK).format(budget)

        // TODO: James Hughes 22 Apr 2012 - need to get a list of all participating orgs
        // TODO: James Hughes 22 Apr 2012 - need to get a list of all countries
        // TODO: James Hughes 22 Apr 2012 - need to get a list of all regions
        // TODO: James Hughes 22 Apr 2012 - need to get a list of all sectors

        val bean = Map(
          "id"              -> id,
          "title"           -> doc.getAs[BSONString]("title").get.value,
          "description"     -> doc.getAs[BSONString]("description").get.value,
          "status"          -> Statuses.get(doc.getAs[BSONLong]("status").get.value).get,
          "budget"          -> budget,
          "formattedBudget" -> formattedBudget.substring(0, formattedBudget.size - 3),
          "organizations"   -> (doc.getAs[BSONString]("organisation").get.value :: Nil).distinct.filterNot(_ == "UNITED KINGDOM").mkString("#"),
          "countries"       -> Nil.mkString("#"),
          "regions"         -> Nil.mkString("#"),
          "sectors"         -> Nil.mkString("#"),
          "reporting"       -> doc.getAs[BSONString]("organisation").get.value,
          "end-date"        -> chooseBetterDate( doc.getAs[BSONDateTime]("end-actual"), doc.getAs[BSONDateTime]("end-planned")),
          "start-date"      -> chooseBetterDate(doc.getAs[BSONDateTime]("start-actual"),doc.getAs[BSONDateTime]("start-planned"))
        )

        ElasticSearch.index(bean, "aid")
      }
    }
  }

  private def indexCountrySuggestions = {
    for (
      countries <- db.collection("countries").find(BSONDocument()).toList;
      stats <- db.collection("country-stats").find(BSONDocument(
        "totalBudget" -> BSONDocument(
          "$gt" -> BSONLong(0L)
        )
      )).toList
    ) yield {
      stats.map {
        stat =>
          val code = stat.getAs[BSONString]("code").get.value
          val name = countries.find(_.getAs[BSONString]("code").get.value == code).head.getAs[BSONString]("name").get.value
          val bean = Map(
            // this is called sugestion - i.e. badly named for some temporary backwards compatibility
            "sugestion"     -> "CountriesSugestion",
            "countryName"   -> name,
            "countryCode"   -> code,
            "countryBudget" -> stat.getAs[BSONLong]("totalBudget").map(_.value).getOrElse(0L)
          )

          ElasticSearch.index(bean, "aid")
      }
    }
  }

  private def indexDfidProjects = {

    // touching components here as it will perform the load into memory.
    // with a full dataset this might be pretty huge adn we should address
    // that ASAP
    components

    val projects = Await.result(db.collection("projects").find(BSONDocument()).toList(), Duration.Inf)

    // loop over projects collection and index the values
    projects.map { doc =>
        val id = doc.getAs[BSONString]("iatiId").get.value
        val budget = doc.getAs[BSONLong]("totalBudget").map(_.value).getOrElse(0L)
        val formattedBudget = NumberFormat.getCurrencyInstance(Locale.UK).format(budget)
        val component = components(id)

        val organisations = doc.getAs[BSONArray]("participatingOrgs").map {
          values =>
            values.values.toList.flatMap {
              case value =>
                value match {
                  case v: BSONString => Some(v.value)
                  case _ => None
                }
            }
        }.getOrElse(List.empty)

        val bean = Map(
          "id"              -> id,
          "title"           -> doc.getAs[BSONString]("title").get.value,
          "description"     -> doc.getAs[BSONString]("description").get.value,
          "status"          -> Statuses.get(doc.getAs[BSONInteger]("status").get.value).get,
          "budget"          -> budget,
          "formattedBudget" -> formattedBudget.substring(0, formattedBudget.size - 3),
          "organizations"   -> (organisations ::: component("organizations")).distinct.filterNot(_ == "UNITED KINGDOM").mkString("#"),
          "subActivities"   -> component("subActivities").mkString("#"),
          "countries"       -> component("countries").mkString("#"),
          "regions"         -> component("regions").mkString("#"),
          "sectors"         -> component("sectors").mkString("#"),
          "reporting"       -> "Department for International Development",
          "end-date"        -> chooseBetterDate( doc.getAs[BSONDateTime]("end-actual"), doc.getAs[BSONDateTime]("end-planned")),
          "start-date"      -> chooseBetterDate(doc.getAs[BSONDateTime]("start-actual"),doc.getAs[BSONDateTime]("start-planned"))
        )

        ElasticSearch.index(bean, "aid")
    }

  }

  private  def  chooseBetterDate(actual: Option[BSONDateTime], planned: Option[BSONDateTime]) : String = {

    if(actual != Nil || !actual.isEmpty)
      return actual.map(_.value).mkString;
    if(planned!=Nil || !actual.isEmpty)
      return planned.map(_.value).mkString;

    return "0";
  }

}
