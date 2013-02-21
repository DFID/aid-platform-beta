package models

import reactivemongo.bson.{BSONLong, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import play.api.data._
import play.api.data.Forms._

case class WhatWeDoEntry(id: Option[BSONObjectID], title: String, value: Long)


object WhatWeDoEntry {

  implicit object WhatWeDoEntryReader extends BSONReader[WhatWeDoEntry]{
    def fromBSON(doc: BSONDocument): WhatWeDoEntry = {

      val document = doc.toTraversable

      WhatWeDoEntry(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("title").map(_.value).get,
        document.getAs[BSONLong]("value").map(_.value).get
      )

    }
  }

  implicit object WhatWeDoEntryWriter extends BSONWriter[WhatWeDoEntry]{
    def toBSON(document: WhatWeDoEntry): BSONDocument = {

      BSONDocument(
        "_id" -> document.id.getOrElse(BSONObjectID.generate),
        "title" -> BSONString(document.title),
        "value" -> BSONLong(document.value)
      )

    }
  }

  val form = Form(
    mapping(
      "id" -> ignored[Option[BSONObjectID]](None),
      "title" -> text,
      "value" -> longNumber
    )(WhatWeDoEntry.apply)
     (WhatWeDoEntry.unapply)
  )
}