package lib

import com.google.inject.Inject
import reactivemongo.api.{DefaultCollection, DefaultDB}
import reactivemongo.bson.{BSONObjectID, BSONDocument, BSONString}
import play.api.libs.iteratee.Enumerator
import traits.FrontPageManagedContentApi
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global

class MongoBackedFrontPageManagedContentApi @Inject()(db: DefaultDB) extends FrontPageManagedContentApi {

  private lazy val whatwedo = db.collection("whatwedo")
  private lazy val whatweachieve = db.collection("whatweachieve")

  def getWhatWeDo = getTop5(whatwedo)

  def getWhatWeAchieve = getTop5(whatweachieve)

  def saveWhatWeDo(entries: List[(String, String)]) = saveTop5(whatwedo, entries)

  def saveWhatWeAchieve(entries: List[(String, String)]) = saveTop5(whatweachieve, entries)

  private def saveTop5(collection: DefaultCollection, entries: List[(String, String)]) = {
    collection.drop.onComplete { case _ =>
      val top5 = entries.take(5).map { case (title, value) =>
        BSONDocument(
          "_id"   -> BSONObjectID.generate,
          "title" -> BSONString(title),
          "value" -> BSONString(value)
        )
      }

      println(s"Inserting documents $top5")
      collection.insert(Enumerator.enumerate(top5), 5)
    }
  }

  private def getTop5(collection: DefaultCollection) = {
    // items are inserted in batch and will always come back in the correct order
    collection.find(BSONDocument()).toList(5).map { results =>

    // map the results to key value pairs
      val actual = results.map { entry =>
        val text  = entry.getAs[BSONString]("title").map(_.value).get
        val value = entry.getAs[BSONString]("value").map(_.value).get

        text -> value
      }

      // pad out the remaining results with empty pairs
      // size to 4 due to ranges being inclusive
      actual ++ actual.size.to(4).map(_ => "" -> "")
    }
  }
}
