package uk.gov.dfid.common.api

import uk.gov.dfid.common.models.Project
import reactivemongo.bson.{BSONObjectID, BSONString, BSONDocument}
import reactivemongo.api.{DefaultDB, SortOrder, QueryBuilder}
import com.google.inject.Inject
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.api.indexes.{IndexType, Index}
import concurrent.ExecutionContext.Implicits.global

class ReadOnlyProjectsApi @Inject()(database: DefaultDB) extends ReadOnlyApi[Project]{

  lazy val projects = database.collection("projects")
  implicit val reader = Project.ProjectReader

  def all = {
    projects.find(QueryBuilder().sort("iatiId" -> SortOrder.Ascending)).toList
  }

  def get(id: String)  = {
    projects.find(BSONDocument("iatiId" -> BSONString(id))).headOption
  }

  def query(criteria: BSONDocument) = {
    projects.find(criteria).toList
  }
}

class ProjectsApi @Inject()(database: DefaultDB) extends ReadOnlyProjectsApi(database) with Api[Project]  {

  projects.indexesManager.create(
    Index("iatiId" -> IndexType.Ascending :: Nil, unique = true)
  )

  def insert(model: Project) = {
    val id = BSONObjectID.generate
    projects.insert(model.copy(id = Some(id))).map(_ => id)
  }

  def update(id: String, model: Project) {
    get(id).map { maybeCountry =>
      maybeCountry.map { country =>
        projects.update(
          BSONDocument("iatiId" -> BSONString(id)),
          // these apis are only
          model.copy(
            id = country.id
          ),
          multi = false,
          upsert = false
        )
      }
    }
  }

  def delete(id: String) {
    projects.remove(BSONDocument("iatiId" -> BSONString(id)))
  }
}
