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

object Main extends App  {

  val config    = ConfigFactory.load("application.conf")
  val names     = ConfigFactory.load("countries.conf")
  val mongo     = MongoConnection(config.getStringList("mongodb.servers").toList)
  val database  = mongo.db(config.getString("mongodb.db"))
  val countries = database.collection("countries")
  val regions   = database.collection("regions")

  // create an index to avoid dirty data
  countries.indexesManager.create(
    Index("code" -> IndexType.Ascending :: Nil, unique = true)
  )

  println("Loading Collection of Countries")
  loadCountries
  println("Loading Collection of Regions")
  loadRegions
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
}