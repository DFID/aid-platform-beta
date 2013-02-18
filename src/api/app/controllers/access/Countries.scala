package controllers.access

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import org.neo4j.graphdb.GraphDatabaseService

class Countries @Inject()(db: GraphDatabaseService) extends Controller {

  def index = TODO
}
