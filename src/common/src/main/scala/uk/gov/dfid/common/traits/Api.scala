package uk.gov.dfid.common.traits

import concurrent.Future
import reactivemongo.bson.{BSONDocument, BSONObjectID}

trait Api[T] extends ReadOnlyApi[T] {
  def insert(model: T): Future[BSONObjectID]
  def update(id: String, model: T): Unit
  def delete(id: String)
}

trait ReadOnlyApi[T] {
  def all: Future[List[T]]
  def get(id: String): Future[Option[T]]
  def query(criteria: BSONDocument): Future[List[T]]
}