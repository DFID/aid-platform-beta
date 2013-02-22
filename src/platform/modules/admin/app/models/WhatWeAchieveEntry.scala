package models

import reactivemongo.bson.{BSONLong, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import play.api.data._
import play.api.data.Forms._

case class WhatWeAchieveEntry(id: Option[BSONObjectID], title: String, value: Long)


object WhatWeAchieveEntry {

  implicit object WhatWeAchieveEntryReader extends BSONReader[WhatWeAchieveEntry]{
    def fromBSON(doc: BSONDocument): WhatWeAchieveEntry = {

      val document = doc.toTraversable

      WhatWeAchieveEntry(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("title").map(_.value).get,
        document.getAs[BSONLong]("value").map(_.value).get
      )

    }
  }

  implicit object WhatWeAchieveEntryWriter extends BSONWriter[WhatWeAchieveEntry]{
    def toBSON(document: WhatWeAchieveEntry): BSONDocument = {

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
    )(WhatWeAchieveEntry.apply)
      (WhatWeAchieveEntry.unapply)
  )
}