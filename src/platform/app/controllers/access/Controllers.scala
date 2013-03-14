package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import com.google.inject.Inject
import org.neo4j.graphdb.{GraphDatabaseService, Node}

import lib.ApiController


class Activities @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  import lib.JsonWriters.DefaultNodeWrites

  def index = Action { implicit request =>
    val (options, results) = list("iati-activity")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }

  def view(id: String) = Action {
    Ok(Json.toJson(single("iati-activity", id)))
  }
}

class Transactions @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  import lib.JsonWriters.DefaultNodeWrites

  def index = Action { implicit request =>
    val (options, results) = list("transaction", "label")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }
}

class Organisations @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  import lib.JsonWriters.DefaultNodeWrites

  def index = Action { implicit request =>
    val (options, results) = list("iati-organisation")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }

  def view(id: String) = Action {
    Ok(Json.toJson(single("iati-organisation", id)))
  }
}

