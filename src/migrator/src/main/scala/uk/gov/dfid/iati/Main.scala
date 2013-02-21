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

  // create an index to avoid dirty data
  countries.indexesManager.create(
    Index("code" -> IndexType.Ascending :: Nil, unique = true)
  )

  println("Dropping Collection of Countries")
  val action = countries.drop map { case _ =>

    println("Seeding DB")

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

      Await.ready(countries.insert(document), 5 seconds)

      println(s"Inserted ${code}:${name}")
    }

    Await.ready(action, 2 minutes)
    println("Shutting down Mongo")
    MongoConnection.system.shutdown()
    println("Exiting")
  }
}