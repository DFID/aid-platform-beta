package controllers.api

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import collection.JavaConversions._
import com.google.inject.Inject
import org.neo4j.graphdb.GraphDatabaseService
import lib.JsonWriters._

class Activities @Inject()(db: GraphDatabaseService )extends Controller {

  def index = Action {
    val results = db.index.forNodes("entities").get("type", "iati-activity")
    Ok(Json.toJson(results.iterator.toSeq))
  }

}

