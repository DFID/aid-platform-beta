package lib

import controllers.{routes, WhatWeDo}
import models.WhatWeDoEntry
import uk.gov.dfid.common.api.Api
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import concurrent.Future
import com.google.inject.Inject
import reactivemongo.api.DefaultDB
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import concurrent.ExecutionContext.Implicits.global

class WhatWeDoApi @Inject()(database: DefaultDB)  extends Api[WhatWeDoEntry]{

  lazy val entries = database.collection("whatwedo")

  def insert(model: WhatWeDoEntry): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    entries.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: WhatWeDoEntry) {
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

  def all: Future[List[WhatWeDoEntry]] = {
    implicit val reader = WhatWeDoEntry.WhatWeDoEntryReader
    entries.find(BSONDocument()).toList
  }

  def get(id: String): Future[Option[WhatWeDoEntry]] = {
    implicit val reader = WhatWeDoEntry.WhatWeDoEntryReader
    entries.find(BSONDocument("_id" -> BSONObjectID(id))).headOption
  }

  def query(criteria: BSONDocument): Future[List[WhatWeDoEntry]] = ???
}
