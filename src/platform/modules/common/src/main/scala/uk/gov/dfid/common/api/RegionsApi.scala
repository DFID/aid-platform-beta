package uk.gov.dfid.common.api

import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.api.indexes.{IndexType, Index}
import uk.gov.dfid.common.models.{Region, CountryStats, Country}
import reactivemongo.api.{SortOrder, QueryBuilder, DefaultDB}
import com.google.inject.Inject

class ReadOnlyRegionsApi @Inject()(database: DefaultDB) extends ReadOnlyApi[Region] {
  lazy val regions = database.collection("regions")

  implicit val reader = Region.RegionReader

  def all = {
    regions.find(QueryBuilder().sort("code" -> SortOrder.Ascending)).toList
  }

  def get(id: String) = {
    regions.find(BSONDocument("code" -> BSONString(id))).headOption
  }

  def query(criteria: BSONDocument)  = {
    regions.find(criteria).toList
  }

}

class RegionsApi @Inject()(database: DefaultDB)  extends ReadOnlyRegionsApi(database) with Api[Region]  {

  regions.indexesManager.create(
    Index("code" -> IndexType.Ascending :: Nil, unique = true)
  )

  def insert(model: Region): Future[BSONObjectID] = {
    val id = BSONObjectID.generate
    regions.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: Region) {
    get(id).map { maybeRegion =>
      maybeRegion.map { region =>
        regions.update(
          BSONDocument("code" -> BSONString(id)),
          // these apis are only
          model.copy(id = region.id),
          multi = false,
          upsert = false
        )
      }
    }
  }

  def delete(id: String) {
    regions.remove(BSONDocument("code" -> BSONString(id)))
  }
}
