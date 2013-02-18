package lib

import traits.{MongoAccess, Api}
import models.Country
import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter

class CountriesApi extends Api[Country] with MongoAccess {

  lazy val countries = database("countries")

  def insert(model: Country): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    countries.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: Country) {
    val _id = BSONObjectID(id)
    countries.update(
      BSONDocument("_id" -> _id),
      model.copy(id = Some(_id)),
      multi = false,
      upsert = false
    )
  }

  def delete(id: String) {
    countries.remove(BSONDocument("_id" -> BSONObjectID(id)))
  }

  def all: Future[List[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(BSONDocument()).toList
  }

  def get(id: String): Future[Option[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(BSONDocument("_id" -> BSONObjectID(id))).headOption
  }

  def query(criteria: BSONDocument): Future[List[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(criteria).toList
  }
}
