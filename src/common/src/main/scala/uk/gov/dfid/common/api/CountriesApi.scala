package uk.gov.dfid.common.api

import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.api.indexes.{IndexType, Index}
import uk.gov.dfid.common.traits.{Api, ReadOnlyApi}
import uk.gov.dfid.common.models.Country
import reactivemongo.api.DefaultDB
import com.google.inject.Inject

class ReadOnlyCountriesApi @Inject()(database: DefaultDB) extends ReadOnlyApi[Country] {
  lazy val countries = database.collection("countries")

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

class CountriesApi @Inject()(database: DefaultDB)  extends ReadOnlyCountriesApi(database) with Api[Country]  {

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
}
