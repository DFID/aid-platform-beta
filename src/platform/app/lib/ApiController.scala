package lib

import play.api.mvc.Controller
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.ExecutionEngine
import play.api.data._
import play.api.data.Forms._


trait ApiController { self: Controller =>

  def engine(implicit graph: GraphDatabaseService) = new ExecutionEngine(graph)

  val paging = Form(
    tuple(
      "start" -> default(number, 0),
      "limit" -> default(number, 100)
    )
  )

  def get
}
