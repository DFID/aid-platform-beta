package uk.gov.dfid.loader.util

import reactivemongo.api.DefaultDB
import com.google.inject.Inject
import reactivemongo.bson.{BSONString, BSONLong, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.Await
import concurrent.duration.Duration
import concurrent.ExecutionContext.Implicits.global

class Sectors(db: DefaultDB) {

  def getHighLevelSector(thirdLevelCode: Long): String = {
    val name = db.collection("sector-hierarchies").find(BSONDocument(
      "sectorCode" -> BSONLong(thirdLevelCode)
    )).headOption.map { maybeThing =>
      maybeThing.map(_.getAs[BSONString]("highLevelName").map(_.value).get).getOrElse("")
    }

    Await.result(name, Duration.Inf)
  }
}