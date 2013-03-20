package uk.gov.dfid.iati

import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoConnection
import collection.JavaConversions._
import xml.XML
import java.net.URL
import reactivemongo.bson.{BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import io.Source
import reactivemongo.api.indexes.{IndexType, Index}
import concurrent.Await
import concurrent.duration._
import util.parsing.combinator.RegexParsers

object Main extends App  {

  val config    = ConfigFactory.load("application.conf")
  val names     = ConfigFactory.load("countries.conf")
  val mongo     = MongoConnection(config.getStringList("mongodb.servers").toList)
  val database  = mongo.db(config.getString("mongodb.db"))
  val countries = database.collection("countries")
  val regions   = database.collection("regions")
  val country_results = database.collection("country-results")
  val sector_hierarchies = database.collection("sector-hierarchies")
  val country_results_src = Source.fromURL(getClass.getResource("/country_results.csv"))
  val sector_hierarchies_src = Source.fromURL(getClass.getResource("/sector_hierarchies.csv"))

  // create an index to avoid dirty data
  countries.indexesManager.create(
    Index("code" -> IndexType.Ascending :: Nil, unique = true)
  )

  println("Loading Collection of Countries")
  loadCountries
  println("Loading Collection of Regions")
  loadRegions
  println("Loading Collection of country results")
  loadCountryResults
  println("Loading Collection of sector hierarchies")
  loadSectorHierarchies
  println("Shutting down Mongo")
  MongoConnection.system.shutdown()
  println("Exiting")

  private def loadRegions = {
    Await.ready(regions.drop(), Duration.Inf)

    println("Seeding Regions")

    val xml = XML.load(new URL("http://datadev.aidinfolabs.org/data/codelist/Region.xml"))

    (xml \\ "Region") map { node =>
      val code = (node \ "code").text
      val name = (node \ "name").text.replace(", regional", "")

      val document = BSONDocument(
        "code" -> BSONString(code),
        "name" -> BSONString(name)
      )

      Await.ready(regions.insert(document), Duration.Inf)

      println(s"Inserted ${code}:${name}")
    }

    Seq(
      "BL" -> "Balkan Regional",
      "EA" -> "East Africa",
      "IB" -> "Indian Ocean Asia Regional",
      "LE" -> "Latin America Regional",
      "EB" -> "East African Community",
      "EF" -> "EECAD Regional",
      "ED" -> "East Europe Regional",
      "FA" -> "Francophone Africa",
      "CP" -> "Central Africa Regional",
      "OT" -> "Overseas Territories",
      "SQ" -> "South East Asia"
    ).foreach { case (code, name) =>
      val document = BSONDocument(
        "code" -> BSONString(code),
        "name" -> BSONString(name)
      )

      Await.ready(regions.insert(document), Duration.Inf)

      println(s"Inserted ${code}:${name}")
    }

  }
  private def loadCountries = {
    Await.ready(countries.drop(), Duration.Inf)

    println("Seeding Countries")

    val xml = XML.load(new URL("http://datadev.aidinfolabs.org/data/codelist/Country.xml"))

    (xml \\ "Country") map { node =>
    // derive the ISO Country code
      val code = (node \ "code").text

      // derive the name either from IATI or the DFID standard
      val name = if (names.hasPath(code))  {
        names.getString(code)
      } else {
        (node \ "name").text.toLowerCase.capitalize
      }

      // load any potential markdown
      val resource = getClass.getResource("/countryinfo/%s.md".format(code))
      val description = resource match {
        case null => None
        case res => Some("description" -> BSONString(Source.fromURL(res).mkString))
      }

      val document = BSONDocument(
        "code" -> BSONString(code),
        "name" -> BSONString(name)
      ).append(
        Seq(description).flatten: _*
      )

      Await.ready(countries.insert(document), Duration.Inf)

      println(s"Inserted ${code}:${name}")
    }
  }

  private def loadCountryResults = {
    Await.ready(country_results.drop(), Duration.Inf)
    val source = country_results_src.getLines.drop(1).mkString("\n")
    val results = CSV.parse(source)
    results.foreach(result => {
      val document = BSONDocument(
        "country" -> BSONString(result(0)),
        "code" -> BSONString(result(1)),
        "pillar" -> BSONString(result(2)),
        "results" -> BSONString(result(3)),
        "total" -> BSONString(result(4))
      )

      Await.ready(country_results.insert(document), Duration.Inf)
      println(s"Inserted document! ")
    })
  }

  private def loadSectorHierarchies = {
    Await.ready(sector_hierarchies.drop(), Duration.Inf)
    val source = sector_hierarchies_src.getLines.drop(1).mkString("\n")
    val sectors = CSV.parse(source)
    sectors.foreach(sector => {
      val document = BSONDocument(
        "highLevelCode" -> BSONString(sector(1)),
        "highLevelName" -> BSONString(sector(2)),
        "categoryCode"  -> BSONString(sector(5)),
        "categoryName"  -> BSONString(sector(6)),
        "sectorCode"    -> BSONString(sector(0)),
        "sectorName"    -> BSONString(sector(3)),
        "sectorDesc"    -> BSONString(sector(4))
      )

      Await.ready(sector_hierarchies.insert(document), Duration.Inf)
      println(s"Sector hierarchy element inserted!")
    })
  }

  object CSV extends RegexParsers {
    override val skipWhitespace = false   // meaningful spaces in CSV

    def COMMA   = ","
    def DQUOTE  = "\""
    def DQUOTE2 = "\"\"" ^^ { case _ => "\"" }  // combine 2 dquotes into 1
    def CRLF    = "\r\n" | "\n"
    def TXT     = "[^\",\r\n]".r
    def SPACES  = "[ \t]+".r

    def file: Parser[List[List[String]]] = repsep(record, CRLF) <~ (CRLF?)

    def record: Parser[List[String]] = repsep(field, COMMA)

    def field: Parser[String] = escaped|nonescaped

    def escaped: Parser[String] = {
      ((SPACES?)~>DQUOTE~>((TXT|COMMA|CRLF|DQUOTE2)*)<~DQUOTE<~(SPACES?)) ^^ {
        case ls => ls.mkString("")
      }
    }

    def nonescaped: Parser[String] = (TXT*) ^^ { case ls => ls.mkString("") }

    def parse(s: String) = parseAll(file, s) match {
      case Success(res, _) => res
      case e => throw new Exception(e.toString)
    }
  }
}