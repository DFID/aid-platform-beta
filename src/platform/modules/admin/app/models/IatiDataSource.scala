package models

import reactivemongo.bson.{BSONBoolean, BSONString, BSONDocument, BSONObjectID}
import reactivemongo.bson.handlers.{BSONReader, BSONWriter}

case class IatiDataSource(id: Option[BSONObjectID], sourceType: String, title: String, url: String, active: Boolean)

object IatiDataSource {

  implicit object IatiDataSourceWriter extends BSONWriter[IatiDataSource]{
    def toBSON(organisation: IatiDataSource): BSONDocument = {
      BSONDocument(
        "_id"        -> organisation.id.getOrElse(BSONObjectID.generate),
        "sourceType" -> BSONString(organisation.sourceType),
        "title"      -> BSONString(organisation.title),
        "url"        -> BSONString(organisation.url),
        "active"     -> BSONBoolean(organisation.active)
      )
    }
  }

  implicit object IatiDataSourceReader extends BSONReader[IatiDataSource] {
    def fromBSON(document: BSONDocument): IatiDataSource = {
      val doc = document.toTraversable
      IatiDataSource(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("sourceType").map(_.value).get,
        doc.getAs[BSONString]("title").map(_.value).get,
        doc.getAs[BSONString]("url").map(_.value).get,
        doc.getAs[BSONBoolean]("active").map(_.value).get
      )
    }
  }
}