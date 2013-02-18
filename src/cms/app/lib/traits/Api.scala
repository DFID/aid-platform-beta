package lib.traits

import concurrent.Future
import reactivemongo.bson.{BSONObjectID, BSONDocument}

trait Api[T] {
  def insert(model: T): Future[BSONObjectID]
  def update(id: String, model: T): Unit
  def delete(id: String)
  def all: Future[List[T]]
  def get(id: String): Future[Option[T]]
  def query(criteria: BSONDocument): Future[List[T]]
}