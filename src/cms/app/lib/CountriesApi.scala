package lib

import traits.{MongoAccess, Api}
import models.Country
import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.api.indexes.{IndexType, Index}

class CountriesApi extends Api[Country] with MongoAccess {

  lazy val countries = database("countries")

  countries.indexesManager.create(
    Index("code" -> IndexType.Ascending :: Nil, unique = true)
  )

  def insert(model: Country): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    countries.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: Country) {
    get(id).map { maybeCountry =>
      maybeCountry.map { country =>
        countries.update(
          BSONDocument("code" -> BSONString(id)),
          model.copy(id = country.id),
          multi = false,
          upsert = false
        )
      }
    }
  }

  def delete(id: String) {
    countries.remove(BSONDocument("code" -> BSONString(id)))
  }

  def all: Future[List[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(BSONDocument()).toList
  }

  def get(id: String): Future[Option[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(BSONDocument("code" -> BSONString(id))).headOption
  }

  def query(criteria: BSONDocument): Future[List[Country]] = {
    implicit val reader = Country.CountryReader
    countries.find(criteria).toList
  }
}
