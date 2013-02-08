package lib

import reactivemongo.bson.BSONObjectID
import concurrent.Future
import models.IatiDataSource

trait SourceSelector {
  def get(sourceType: String, activeOnly: Boolean = false): Future[List[IatiDataSource]]
  def activate(sourceType: String, ids: String*)
  def load(sourceType: String): Future[_]
}
