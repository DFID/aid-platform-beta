package lib.impl

import models.IatiDataSource
import reactivemongo.bson._
import models.IatiDataSource._
import play.api.libs.ws.WS
import play.api.libs.iteratee.Enumerator
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import play.api.libs.json.JsArray
import reactivemongo.bson.BSONBoolean
import reactivemongo.bson.BSONString
import lib.{MongoAccess, SourceSelector}
import play.api.Logger

class IatiDataSourceSelector extends SourceSelector with MongoAccess {

  private val datasources = database.collection("iati-datasources")
  private val logger = Logger.logger

  def get(sourceType: String, activeOnly: Boolean) = {
    val query = BSONDocument(Seq(
      Some("sourceType" -> BSONString(sourceType)),
      if(activeOnly)
        Some("active" -> BSONBoolean(true))
      else
        None
    ).flatten: _*)

    logger.debug(s"Querying DB for ${query.elements.map(e => e.name + "=" + e.value.toString).mkString(",")}")

    datasources.find(query).toList
  }

  def activate(sourceType: String, ids: String*) {
    datasources.update(
      BSONDocument(
        "sourceType" -> BSONString(sourceType)
      ),
      BSONDocument(
        "$set" -> BSONDocument("active" -> BSONBoolean(false))
      ),
      upsert = false,
      multi = true
    ) onComplete { case _ =>
      datasources.update(
        BSONDocument(
          "_id" -> BSONDocument(
            "$in" -> BSONArray(ids.map(id => BSONObjectID(id)): _*)
          )
        ),
        BSONDocument(
          "$set" -> BSONDocument("active" -> BSONBoolean(true))
        ),
        upsert = false,
        multi = true
      )
    }
  }

  /**
   * The load process is a bit complex.  First of all we need to extract all
   * the current organisations and get all the active URLs.  Then we hit the
   * IATI Registry endpoint and parse all the provider files.  Any of the
   * currently active URLs are as active again.  The DB collection is
   * dropped and recreated again.
   * @return
   */
  def load(sourceType: String) = {
    datasources.find(
      BSONDocument("sourceType" -> BSONString(sourceType))
    ).toList.map(_.filter(_.active).map(_.url)).flatMap { list =>

      val url = s"http://www.iatiregistry.org/api/search/dataset?filetype=$sourceType&all_fields=1&limit=4000"

      WS.url(url).get.flatMap { response =>
        val orgs = (response.json \ "results").as[JsArray].value.map { json =>
          val url = (json \ "download_url").as[String]
          val title = (json \ "title").as[String]
          IatiDataSource(None, sourceType, title, url, list.contains(url))
        }

        datasources.remove(
          BSONDocument("sourceType" -> BSONString(sourceType)),
          firstMatchOnly = false
        ).flatMap { _ =>
          datasources.insert(Enumerator(orgs: _*), orgs.size)
        }
      }
    }

  }
}
