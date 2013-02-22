package lib

import uk.gov.dfid.common.api.Api
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import concurrent.Future
import com.google.inject.Inject
import reactivemongo.api.DefaultDB
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import concurrent.ExecutionContext.Implicits.global
import models.WhatWeAchieveEntry


class WhatWeAchieveApi @Inject()(database: DefaultDB) extends Api[WhatWeAchieveEntry]{

  lazy val entries = database.collection("whatweachieve")

  def insert(model: WhatWeAchieveEntry): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    entries.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: WhatWeAchieveEntry) {
    entries.update(
      BSONDocument("_id" -> BSONObjectID(id)),
      model.copy(
        id = Some(BSONObjectID(id))
      ),
      upsert = false,
      multi = false
    )
  }

  def delete(id: String) {
    entries.remove(
      BSONDocument("_id" -> BSONObjectID(id)),
      firstMatchOnly = true
    )
  }

  def all: Future[List[WhatWeAchieveEntry]] = {
    implicit val reader = WhatWeAchieveEntry.WhatWeAchieveEntryReader
    entries.find(BSONDocument()).toList
  }

  def get(id: String): Future[Option[WhatWeAchieveEntry]] = {
    implicit val reader = WhatWeAchieveEntry.WhatWeAchieveEntryReader
    entries.find(BSONDocument("_id" -> BSONObjectID(id))).headOption
  }

  def query(criteria: BSONDocument): Future[List[WhatWeAchieveEntry]] = ???
}
